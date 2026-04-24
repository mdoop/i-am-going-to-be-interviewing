package com.example.retail.kotlinapp.cache

import com.example.retail.contract.PriceQuoteRequest

object CacheKey {
    fun from(request: PriceQuoteRequest): String {
        val coupon = request.couponCode()
            ?.takeIf { it.isNotBlank() }
            ?.trim()
            ?.uppercase()
            ?: "NONE"

        return listOf(
            "quote",
            sanitize(request.sku()),
            sanitize(request.storeId()),
            request.channel().name,
            request.customerSegment().name,
            request.quantity().toString(),
            sanitize(coupon),
        ).joinToString(":")
    }

    private fun sanitize(value: String): String = value.replace(":", "_").trim().uppercase()
}

