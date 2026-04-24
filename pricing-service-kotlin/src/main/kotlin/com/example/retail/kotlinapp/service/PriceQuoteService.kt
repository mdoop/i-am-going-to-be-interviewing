package com.example.retail.kotlinapp.service

import com.example.retail.contract.PriceQuoteRequest
import com.example.retail.contract.PriceQuoteResponse
import com.example.retail.contract.PriceSource
import com.example.retail.kotlinapp.cache.CacheKey
import com.example.retail.kotlinapp.cache.QuoteComputation
import com.example.retail.kotlinapp.cache.RedisBackedQuoteCache
import com.example.retail.kotlinapp.error.ProductNotFoundException
import com.example.retail.kotlinapp.repository.ProductCatalogRepository
import com.example.retail.kotlinapp.repository.PromotionRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import kotlin.math.roundToLong

@Service
class PriceQuoteService(
    private val productCatalogRepository: ProductCatalogRepository,
    private val promotionRepository: PromotionRepository,
    private val priceQuoteCalculator: PriceQuoteCalculator,
    private val quoteCache: RedisBackedQuoteCache,
    private val meterRegistry: MeterRegistry,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun quote(request: PriceQuoteRequest): PriceQuoteResponse {
        val sample = Timer.start(meterRegistry)
        val cacheKey = CacheKey.from(request)
        val traceId = MDC.get("traceId") ?: "missing-trace-id"

        return quoteCache.find(cacheKey)?.let { computation ->
            val latencyMs = stopTimer(sample, "CACHE")
            log.info("quote_cache_hit sku={} traceId={} latencyMs={}", request.sku(), traceId, latencyMs)
            toResponse(request, computation, PriceSource.CACHE, traceId, latencyMs)
        } ?: computeFreshQuote(request, cacheKey, traceId, sample)
    }

    private fun computeFreshQuote(
        request: PriceQuoteRequest,
        cacheKey: String,
        traceId: String,
        sample: Timer.Sample,
    ): PriceQuoteResponse {
        val product = productCatalogRepository.findBySkuAndStoreId(request.sku(), request.storeId())
            ?: throw ProductNotFoundException(request.sku(), request.storeId())

        val computation = priceQuoteCalculator.compute(product, promotionRepository.findBySku(request.sku()), request)
        quoteCache.store(cacheKey, computation)

        val latencyMs = stopTimer(sample, "DATABASE")
        log.info("quote_computed sku={} traceId={} latencyMs={}", request.sku(), traceId, latencyMs)
        return toResponse(request, computation, PriceSource.DATABASE, traceId, latencyMs)
    }

    private fun toResponse(
        request: PriceQuoteRequest,
        computation: QuoteComputation,
        source: PriceSource,
        traceId: String,
        latencyMs: Long,
    ): PriceQuoteResponse = PriceQuoteResponse(
        request.sku(),
        request.storeId(),
        computation.basePrice,
        computation.appliedPromotions,
        computation.finalPrice,
        computation.fulfillmentOptions,
        source,
        traceId,
        latencyMs,
    )

    private fun stopTimer(sample: Timer.Sample, source: String): Long =
        (sample.stop(Timer.builder("pricing.quote.latency").tag("source", source).register(meterRegistry)) / 1_000_000.0).roundToLong()
}
