package com.example.retail.kotlinapp.cache

import com.example.retail.contract.AppliedPromotion
import com.example.retail.contract.FulfillmentOption
import java.math.BigDecimal

data class QuoteComputation(
    val basePrice: BigDecimal,
    val appliedPromotions: List<AppliedPromotion>,
    val finalPrice: BigDecimal,
    val fulfillmentOptions: List<FulfillmentOption>,
)

