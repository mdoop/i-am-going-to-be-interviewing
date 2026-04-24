package com.example.retail.kotlinapp.service

import com.example.retail.contract.PromotionImportRequest
import com.example.retail.contract.PromotionImportResponse
import com.example.retail.kotlinapp.cache.RedisBackedQuoteCache
import com.example.retail.kotlinapp.repository.PromotionRepository
import org.springframework.stereotype.Service

@Service
class PromotionAdminService(
    private val promotionRepository: PromotionRepository,
    private val quoteCache: RedisBackedQuoteCache,
) {
    fun importPromotions(request: PromotionImportRequest): PromotionImportResponse {
        promotionRepository.replaceAll(request.promotions())
        val refreshedSkus = request.promotions().map { it.sku() }.distinct()
        refreshedSkus.forEach(quoteCache::evictBySku)
        return PromotionImportResponse(request.promotions().size, refreshedSkus)
    }

    fun findBySku(sku: String) = promotionRepository.findBySku(sku)
}

