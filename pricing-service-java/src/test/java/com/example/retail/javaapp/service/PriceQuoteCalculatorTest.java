package com.example.retail.javaapp.service;

import com.example.retail.contract.Channel;
import com.example.retail.contract.CustomerSegment;
import com.example.retail.contract.PriceQuoteRequest;
import com.example.retail.contract.ProductPrice;
import com.example.retail.contract.AppliedPromotion;
import com.example.retail.contract.PromotionRule;
import com.example.retail.contract.PromotionType;
import com.example.retail.javaapp.cache.QuoteComputation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceQuoteCalculatorTest {

    private final PriceQuoteCalculator calculator = new PriceQuoteCalculator();

    @Test
    void appliesCouponAndAutomaticPromotionInPriorityOrder() {
        ProductPrice product = new ProductPrice("SKU-RED-CHAIR", "STORE-100", new BigDecimal("120.00"), "USD", 10, true, true);
        PriceQuoteRequest request = new PriceQuoteRequest("SKU-RED-CHAIR", "STORE-100", Channel.ONLINE, CustomerSegment.LOYALTY, 1, "SAVE15");

        QuoteComputation result = calculator.compute(product, List.of(
                promotion("AUTO", "SKU-RED-CHAIR", PromotionType.PERCENT_OFF, null, new BigDecimal("10.00"), 1, CustomerSegment.ALL, Channel.ANY, null, 50),
                promotion("COUPON", "SKU-RED-CHAIR", PromotionType.AMOUNT_OFF, new BigDecimal("15.00"), null, 1, CustomerSegment.LOYALTY, Channel.ONLINE, "SAVE15", 100)
        ), request);

        assertThat(result.finalPrice()).isEqualByComparingTo("94.50");
        assertThat(result.appliedPromotions()).extracting(AppliedPromotion::id).containsExactly("COUPON", "AUTO");
    }

    @Test
    void rejectsExpiredPromotions() {
        ProductPrice product = new ProductPrice("SKU-RED-CHAIR", "STORE-100", new BigDecimal("120.00"), "USD", 10, true, true);
        PriceQuoteRequest request = new PriceQuoteRequest("SKU-RED-CHAIR", "STORE-100", Channel.ONLINE, CustomerSegment.STANDARD, 1, null);

        QuoteComputation result = calculator.compute(product, List.of(
                new PromotionRule(
                        "EXPIRED",
                        "SKU-RED-CHAIR",
                        "Expired promo",
                        PromotionType.PERCENT_OFF,
                        null,
                        new BigDecimal("25.00"),
                        1,
                        CustomerSegment.ALL,
                        Channel.ANY,
                        null,
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Instant.parse("2024-12-31T00:00:00Z"),
                        99
                )
        ), request);

        assertThat(result.finalPrice()).isEqualByComparingTo("120.00");
        assertThat(result.appliedPromotions()).isEmpty();
    }

    @Test
    void appliesQuantityPromotionWhenThresholdMet() {
        ProductPrice product = new ProductPrice("SKU-BLUE-LAMP", "STORE-100", new BigDecimal("80.00"), "USD", 10, true, false);
        PriceQuoteRequest request = new PriceQuoteRequest("SKU-BLUE-LAMP", "STORE-100", Channel.STORE, CustomerSegment.STANDARD, 3, null);

        QuoteComputation result = calculator.compute(product, List.of(
                promotion("BULK", "SKU-BLUE-LAMP", PromotionType.PERCENT_OFF, null, new BigDecimal("5.00"), 3, CustomerSegment.ALL, Channel.ANY, null, 60)
        ), request);

        assertThat(result.finalPrice()).isEqualByComparingTo("76.00");
        assertThat(result.appliedPromotions()).hasSize(1);
    }

    private PromotionRule promotion(
            String id,
            String sku,
            PromotionType type,
            BigDecimal amountOff,
            BigDecimal percentOff,
            int minQuantity,
            CustomerSegment segment,
            Channel channel,
            String couponCode,
            int priority
    ) {
        return new PromotionRule(
                id,
                sku,
                id + " description",
                type,
                amountOff,
                percentOff,
                minQuantity,
                segment,
                channel,
                couponCode,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2030-01-01T00:00:00Z"),
                priority
        );
    }
}
