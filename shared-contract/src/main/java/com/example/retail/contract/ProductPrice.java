package com.example.retail.contract;

import java.math.BigDecimal;

public record ProductPrice(
        String sku,
        String storeId,
        BigDecimal basePrice,
        String currency,
        int inventoryOnHand,
        boolean shippingEligible,
        boolean pickupEligible
) {
}

