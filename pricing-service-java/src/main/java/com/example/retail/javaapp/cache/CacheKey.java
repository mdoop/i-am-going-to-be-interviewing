package com.example.retail.javaapp.cache;

import com.example.retail.contract.PriceQuoteRequest;

public final class CacheKey {

    private CacheKey() {
    }

    public static String from(PriceQuoteRequest request) {
        String coupon = request.couponCode() == null || request.couponCode().isBlank()
                ? "none"
                : request.couponCode().trim().toUpperCase();

        return "quote:%s:%s:%s:%s:%s:%s".formatted(
                sanitize(request.sku()),
                sanitize(request.storeId()),
                request.channel().name(),
                request.customerSegment().name(),
                request.quantity(),
                sanitize(coupon)
        );
    }

    private static String sanitize(String value) {
        return value.replace(":", "_").trim().toUpperCase();
    }
}

