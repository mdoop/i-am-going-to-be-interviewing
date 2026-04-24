package com.example.retail.contract;

import java.math.BigDecimal;

public record AppliedPromotion(
        String id,
        String description,
        PromotionType type,
        BigDecimal value,
        int priority
) {
}

