package com.example.retail.javaapp.service;

import com.example.retail.contract.PriceQuoteRequest;
import com.example.retail.contract.PriceQuoteResponse;
import com.example.retail.contract.PriceSource;
import com.example.retail.contract.ProductPrice;
import com.example.retail.javaapp.cache.CacheKey;
import com.example.retail.javaapp.cache.QuoteComputation;
import com.example.retail.javaapp.cache.RedisBackedQuoteCache;
import com.example.retail.javaapp.error.ProductNotFoundException;
import com.example.retail.javaapp.repository.ProductCatalogRepository;
import com.example.retail.javaapp.repository.PromotionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class PriceQuoteService {

    private static final Logger log = LoggerFactory.getLogger(PriceQuoteService.class);

    private final ProductCatalogRepository productCatalogRepository;
    private final PromotionRepository promotionRepository;
    private final PriceQuoteCalculator priceQuoteCalculator;
    private final RedisBackedQuoteCache quoteCache;
    private final MeterRegistry meterRegistry;

    public PriceQuoteService(
            ProductCatalogRepository productCatalogRepository,
            PromotionRepository promotionRepository,
            PriceQuoteCalculator priceQuoteCalculator,
            RedisBackedQuoteCache quoteCache,
            MeterRegistry meterRegistry
    ) {
        this.productCatalogRepository = productCatalogRepository;
        this.promotionRepository = promotionRepository;
        this.priceQuoteCalculator = priceQuoteCalculator;
        this.quoteCache = quoteCache;
        this.meterRegistry = meterRegistry;
    }

    public PriceQuoteResponse quote(PriceQuoteRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String cacheKey = CacheKey.from(request);
        String traceId = currentTraceId();

        return quoteCache.find(cacheKey)
                .map(computation -> {
                    long latencyMs = stopTimer(sample, "CACHE");
                    log.info("quote_cache_hit sku={} traceId={} latencyMs={}", request.sku(), traceId, latencyMs);
                    return toResponse(request, computation, PriceSource.CACHE, traceId, latencyMs);
                })
                .orElseGet(() -> computeFreshQuote(request, cacheKey, traceId, sample));
    }

    private PriceQuoteResponse computeFreshQuote(
            PriceQuoteRequest request,
            String cacheKey,
            String traceId,
            Timer.Sample sample
    ) {
        ProductPrice product = productCatalogRepository.findBySkuAndStoreId(request.sku(), request.storeId())
                .orElseThrow(() -> new ProductNotFoundException(request.sku(), request.storeId()));

        QuoteComputation computation = priceQuoteCalculator.compute(
                product,
                promotionRepository.findBySku(request.sku()),
                request
        );
        quoteCache.store(cacheKey, computation);

        long latencyMs = stopTimer(sample, "DATABASE");
        log.info("quote_computed sku={} traceId={} latencyMs={}", request.sku(), traceId, latencyMs);
        return toResponse(request, computation, PriceSource.DATABASE, traceId, latencyMs);
    }

    private PriceQuoteResponse toResponse(
            PriceQuoteRequest request,
            QuoteComputation computation,
            PriceSource source,
            String traceId,
            long latencyMs
    ) {
        return new PriceQuoteResponse(
                request.sku(),
                request.storeId(),
                computation.basePrice(),
                computation.appliedPromotions(),
                computation.finalPrice(),
                computation.fulfillmentOptions(),
                source,
                traceId,
                latencyMs
        );
    }

    private long stopTimer(Timer.Sample sample, String source) {
        return Math.round(sample.stop(Timer.builder("pricing.quote.latency")
                .tag("source", source)
                .register(meterRegistry)) / 1_000_000.0d);
    }

    private String currentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? "missing-trace-id" : traceId;
    }
}

