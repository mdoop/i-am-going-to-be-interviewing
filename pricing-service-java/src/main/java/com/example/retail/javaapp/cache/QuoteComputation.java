package com.example.retail.javaapp.cache;

import com.example.retail.contract.AppliedPromotion;
import com.example.retail.contract.FulfillmentOption;

import java.math.BigDecimal;
import java.util.List;

public record QuoteComputation(
        BigDecimal basePrice,
        List<AppliedPromotion> appliedPromotions,
        BigDecimal finalPrice,
        List<FulfillmentOption> fulfillmentOptions
) {
}

