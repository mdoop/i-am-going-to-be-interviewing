package com.example.retail.kotlinapp.service

import com.example.retail.contract.AppliedPromotion
import com.example.retail.contract.Channel
import com.example.retail.contract.CustomerSegment
import com.example.retail.contract.FulfillmentOption
import com.example.retail.contract.FulfillmentType
import com.example.retail.contract.PriceQuoteRequest
import com.example.retail.contract.ProductPrice
import com.example.retail.contract.PromotionRule
import com.example.retail.contract.PromotionType
import com.example.retail.kotlinapp.cache.QuoteComputation
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Component
class PriceQuoteCalculator {
    fun compute(product: ProductPrice, promotions: List<PromotionRule>, request: PriceQuoteRequest): QuoteComputation {
        val selected = promotions
            .filter { isEligible(it, request) }
            .sortedByDescending { it.priority() }
            .distinctBy { bucket(it) }

        val appliedPromotions = mutableListOf<AppliedPromotion>()
        val finalPrice = selected.fold(product.basePrice()) { runningPrice, rule ->
            appliedPromotions += AppliedPromotion(
                rule.id(),
                rule.description(),
                rule.type(),
                promotionValue(rule),
                rule.priority(),
            )
            applyPromotion(runningPrice, rule)
        }.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return QuoteComputation(
            basePrice = product.basePrice().setScale(2, RoundingMode.HALF_UP),
            appliedPromotions = appliedPromotions,
            finalPrice = finalPrice,
            fulfillmentOptions = fulfillmentOptions(product),
        )
    }

    private fun isEligible(rule: PromotionRule, request: PriceQuoteRequest): Boolean {
        val now = Instant.now()
        val active = !now.isBefore(rule.startsAt()) && !now.isAfter(rule.endsAt())
        val quantityMatch = request.quantity() >= rule.minQuantity()
        val segmentMatch = rule.customerSegment() == CustomerSegment.ALL || rule.customerSegment() == request.customerSegment()
        val channelMatch = rule.channel() == Channel.ANY || rule.channel() == request.channel()
        val couponMatch = rule.couponCode().isNullOrBlank() ||
            (!request.couponCode().isNullOrBlank() && rule.couponCode().equals(request.couponCode(), ignoreCase = true))

        return active && quantityMatch && segmentMatch && channelMatch
            && couponMatch
            && rule.sku() == request.sku()
    }

    private fun bucket(rule: PromotionRule): String = when {
        !rule.couponCode().isNullOrBlank() -> "coupon"
        rule.minQuantity() > 1 -> "quantity"
        else -> "automatic"
    }

    private fun applyPromotion(runningPrice: BigDecimal, rule: PromotionRule): BigDecimal = when (rule.type()) {
        PromotionType.AMOUNT_OFF -> runningPrice.subtract(rule.amountOff() ?: BigDecimal.ZERO)
        PromotionType.PERCENT_OFF -> runningPrice.subtract(
            runningPrice.multiply(rule.percentOff() ?: BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP),
        )
        null -> runningPrice
    }

    private fun promotionValue(rule: PromotionRule): BigDecimal = when (rule.type()) {
        PromotionType.AMOUNT_OFF -> rule.amountOff() ?: BigDecimal.ZERO
        PromotionType.PERCENT_OFF -> rule.percentOff() ?: BigDecimal.ZERO
        null -> BigDecimal.ZERO
    }

    private fun fulfillmentOptions(product: ProductPrice): List<FulfillmentOption> {
        val options = mutableListOf<FulfillmentOption>()
        if (product.pickupEligible() && product.inventoryOnHand() > 0) {
            options += FulfillmentOption(FulfillmentType.STORE_PICKUP, 2)
        }
        if (product.shippingEligible() && product.inventoryOnHand() > 2) {
            options += FulfillmentOption(FulfillmentType.DELIVERY, 48)
        }
        if (options.isEmpty()) {
            options += FulfillmentOption(FulfillmentType.STORE_PICKUP, 24)
        }
        return options
    }
}
