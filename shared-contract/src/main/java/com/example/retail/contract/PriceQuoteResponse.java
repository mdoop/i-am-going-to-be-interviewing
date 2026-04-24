package com.example.retail.contract;

import java.math.BigDecimal;
import java.util.List;

public record PriceQuoteResponse(
        String sku,
        String storeId,
        BigDecimal basePrice,
        List<AppliedPromotion> appliedPromotions,
        BigDecimal finalPrice,
        List<FulfillmentOption> fulfillmentOptions,
        PriceSource pricingSource,
        String traceId,
        long latencyMs
) {
}

