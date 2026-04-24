package com.example.retail.javaapp.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisBackedQuoteCache {

    private static final Logger log = LoggerFactory.getLogger(RedisBackedQuoteCache.class);

    private final boolean redisEnabled;
    private final Duration ttl;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final Map<String, QuoteComputation> fallbackCache = new ConcurrentHashMap<>();

    public RedisBackedQuoteCache(
            @Value("${app.cache.redis-enabled:false}") boolean redisEnabled,
            @Value("${app.cache.ttl-seconds:180}") long ttlSeconds,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.redisEnabled = redisEnabled;
        this.ttl = Duration.ofSeconds(ttlSeconds);
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    public Optional<QuoteComputation> find(String key) {
        if (redisEnabled) {
            try {
                String payload = redisTemplate.opsForValue().get(key);
                if (payload != null) {
                    meterRegistry.counter("pricing.cache.hit", "tier", "redis").increment();
                    return Optional.of(objectMapper.readValue(payload, QuoteComputation.class));
                }
            } catch (Exception ex) {
                meterRegistry.counter("pricing.cache.fallback", "operation", "read").increment();
                log.warn("Redis read failed, falling back to local cache", ex);
            }
        }

        QuoteComputation local = fallbackCache.get(key);
        if (local != null) {
            meterRegistry.counter("pricing.cache.hit", "tier", "local").increment();
            return Optional.of(local);
        }

        meterRegistry.counter("pricing.cache.miss").increment();
        return Optional.empty();
    }

    public void store(String key, QuoteComputation value) {
        fallbackCache.put(key, value);
        if (!redisEnabled) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize cached quote", ex);
        } catch (Exception ex) {
            meterRegistry.counter("pricing.cache.fallback", "operation", "write").increment();
            log.warn("Redis write failed, retaining local cache only", ex);
        }
    }

    public void evictBySku(String sku) {
        String prefix = "quote:%s:".formatted(sku.trim().toUpperCase());
        fallbackCache.keySet().removeIf(key -> key.startsWith(prefix));

        if (!redisEnabled) {
            return;
        }

        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ex) {
            meterRegistry.counter("pricing.cache.fallback", "operation", "evict").increment();
            log.warn("Redis eviction failed for sku={}", sku, ex);
        }
    }
}

