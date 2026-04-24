package com.example.retail.contract;

import java.math.BigDecimal;
import java.time.Instant;

public record PromotionRule(
        String id,
        String sku,
        String description,
        PromotionType type,
        BigDecimal amountOff,
        BigDecimal percentOff,
        int minQuantity,
        CustomerSegment customerSegment,
        Channel channel,
        String couponCode,
        Instant startsAt,
        Instant endsAt,
        int priority
) {
}

