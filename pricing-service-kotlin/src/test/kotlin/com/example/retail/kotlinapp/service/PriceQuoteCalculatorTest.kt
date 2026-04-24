package com.example.retail.kotlinapp.service

import com.example.retail.contract.Channel
import com.example.retail.contract.CustomerSegment
import com.example.retail.contract.PriceQuoteRequest
import com.example.retail.contract.ProductPrice
import com.example.retail.contract.PromotionRule
import com.example.retail.contract.PromotionType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PriceQuoteCalculatorTest {
    private val calculator = PriceQuoteCalculator()

    @Test
    fun `applies coupon and automatic promotion in priority order`() {
        val product = ProductPrice("SKU-RED-CHAIR", "STORE-100", BigDecimal("120.00"), "USD", 10, true, true)
        val request = PriceQuoteRequest("SKU-RED-CHAIR", "STORE-100", Channel.ONLINE, CustomerSegment.LOYALTY, 1, "SAVE15")

        val result = calculator.compute(
            product,
            listOf(
                promotion("AUTO", "SKU-RED-CHAIR", PromotionType.PERCENT_OFF, null, BigDecimal("10.00"), 1, CustomerSegment.ALL, Channel.ANY, null, 50),
                promotion("COUPON", "SKU-RED-CHAIR", PromotionType.AMOUNT_OFF, BigDecimal("15.00"), null, 1, CustomerSegment.LOYALTY, Channel.ONLINE, "SAVE15", 100),
            ),
            request,
        )

        assertThat(result.finalPrice).isEqualByComparingTo("94.50")
        assertThat(result.appliedPromotions.map { it.id() }).containsExactly("COUPON", "AUTO")
    }

    @Test
    fun `rejects expired promotions`() {
        val product = ProductPrice("SKU-RED-CHAIR", "STORE-100", BigDecimal("120.00"), "USD", 10, true, true)
        val request = PriceQuoteRequest("SKU-RED-CHAIR", "STORE-100", Channel.ONLINE, CustomerSegment.STANDARD, 1, null)

        val result = calculator.compute(
            product,
            listOf(
                PromotionRule(
                    "EXPIRED",
                    "SKU-RED-CHAIR",
                    "Expired promo",
                    PromotionType.PERCENT_OFF,
                    null,
                    BigDecimal("25.00"),
                    1,
                    CustomerSegment.ALL,
                    Channel.ANY,
                    null,
                    Instant.parse("2024-01-01T00:00:00Z"),
                    Instant.parse("2024-12-31T00:00:00Z"),
                    99,
                ),
            ),
            request,
        )

        assertThat(result.finalPrice).isEqualByComparingTo("120.00")
        assertThat(result.appliedPromotions).isEmpty()
    }

    @Test
    fun `applies quantity promotion when threshold is met`() {
        val product = ProductPrice("SKU-BLUE-LAMP", "STORE-100", BigDecimal("80.00"), "USD", 10, true, false)
        val request = PriceQuoteRequest("SKU-BLUE-LAMP", "STORE-100", Channel.STORE, CustomerSegment.STANDARD, 3, null)

        val result = calculator.compute(
            product,
            listOf(
                promotion("BULK", "SKU-BLUE-LAMP", PromotionType.PERCENT_OFF, null, BigDecimal("5.00"), 3, CustomerSegment.ALL, Channel.ANY, null, 60),
            ),
            request,
        )

        assertThat(result.finalPrice).isEqualByComparingTo("76.00")
        assertThat(result.appliedPromotions).hasSize(1)
    }

    private fun promotion(
        id: String,
        sku: String,
        type: PromotionType,
        amountOff: BigDecimal?,
        percentOff: BigDecimal?,
        minQuantity: Int,
        segment: CustomerSegment,
        channel: Channel,
        couponCode: String?,
        priority: Int,
    ) = PromotionRule(
        id,
        sku,
        "$id description",
        type,
        amountOff,
        percentOff,
        minQuantity,
        segment,
        channel,
        couponCode,
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2030-01-01T00:00:00Z"),
        priority,
    )
}
