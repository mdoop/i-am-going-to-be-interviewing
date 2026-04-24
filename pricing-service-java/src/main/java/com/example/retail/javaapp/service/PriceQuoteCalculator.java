package com.example.retail.javaapp.service;

import com.example.retail.contract.AppliedPromotion;
import com.example.retail.contract.Channel;
import com.example.retail.contract.CustomerSegment;
import com.example.retail.contract.FulfillmentOption;
import com.example.retail.contract.FulfillmentType;
import com.example.retail.contract.PriceQuoteRequest;
import com.example.retail.contract.ProductPrice;
import com.example.retail.contract.PromotionRule;
import com.example.retail.contract.PromotionType;
import com.example.retail.javaapp.cache.QuoteComputation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PriceQuoteCalculator {

    public QuoteComputation compute(ProductPrice product, List<PromotionRule> promotions, PriceQuoteRequest request) {
        List<PromotionRule> selected = selectPromotions(promotions, request);
        BigDecimal finalPrice = product.basePrice();
        List<AppliedPromotion> applied = new ArrayList<>();

        for (PromotionRule rule : selected) {
            finalPrice = applyPromotion(finalPrice, rule);
            applied.add(new AppliedPromotion(
                    rule.id(),
                    rule.description(),
                    rule.type(),
                    promotionValue(rule),
                    rule.priority()
            ));
        }

        return new QuoteComputation(
                product.basePrice().setScale(2, RoundingMode.HALF_UP),
                applied,
                finalPrice.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                fulfillmentOptions(product)
        );
    }

    private List<PromotionRule> selectPromotions(List<PromotionRule> promotions, PriceQuoteRequest request) {
        Map<String, PromotionRule> winners = new LinkedHashMap<>();

        promotions.stream()
                .filter(rule -> isEligible(rule, request))
                .sorted(Comparator.comparingInt(PromotionRule::priority).reversed())
                .forEach(rule -> winners.putIfAbsent(bucket(rule), rule));

        return winners.values()
                .stream()
                .sorted(Comparator.comparingInt(PromotionRule::priority).reversed())
                .toList();
    }

    private boolean isEligible(PromotionRule rule, PriceQuoteRequest request) {
        Instant now = Instant.now();
        boolean active = !now.isBefore(rule.startsAt()) && !now.isAfter(rule.endsAt());
        boolean skuMatch = rule.sku().equalsIgnoreCase(request.sku());
        boolean quantityMatch = request.quantity() >= rule.minQuantity();
        boolean segmentMatch = rule.customerSegment() == CustomerSegment.ALL
                || rule.customerSegment() == request.customerSegment();
        boolean channelMatch = rule.channel() == Channel.ANY || rule.channel() == request.channel();
        boolean couponMatch = rule.couponCode() == null || rule.couponCode().isBlank()
                || (request.couponCode() != null && rule.couponCode().equalsIgnoreCase(request.couponCode()));

        return active && skuMatch && quantityMatch && segmentMatch && channelMatch && couponMatch;
    }

    private String bucket(PromotionRule rule) {
        if (rule.couponCode() != null && !rule.couponCode().isBlank()) {
            return "coupon";
        }
        if (rule.minQuantity() > 1) {
            return "quantity";
        }
        return "automatic";
    }

    private BigDecimal applyPromotion(BigDecimal runningPrice, PromotionRule rule) {
        return switch (rule.type()) {
            case AMOUNT_OFF -> runningPrice.subtract(nullable(rule.amountOff()));
            case PERCENT_OFF -> runningPrice.subtract(
                    runningPrice.multiply(nullable(rule.percentOff()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            );
        };
    }

    private BigDecimal promotionValue(PromotionRule rule) {
        return rule.type() == PromotionType.AMOUNT_OFF ? nullable(rule.amountOff()) : nullable(rule.percentOff());
    }

    private BigDecimal nullable(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<FulfillmentOption> fulfillmentOptions(ProductPrice product) {
        List<FulfillmentOption> options = new ArrayList<>();
        if (product.pickupEligible() && product.inventoryOnHand() > 0) {
            options.add(new FulfillmentOption(FulfillmentType.STORE_PICKUP, 2));
        }
        if (product.shippingEligible() && product.inventoryOnHand() > 2) {
            options.add(new FulfillmentOption(FulfillmentType.DELIVERY, 48));
        }
        if (options.isEmpty()) {
            options.add(new FulfillmentOption(FulfillmentType.STORE_PICKUP, 24));
        }
        return options;
    }
}
