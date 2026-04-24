package com.example.retail.contract;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PriceQuoteRequest(
        @NotBlank String sku,
        @NotBlank String storeId,
        @NotNull Channel channel,
        @NotNull CustomerSegment customerSegment,
        @Min(1) int quantity,
        String couponCode
) {
}

