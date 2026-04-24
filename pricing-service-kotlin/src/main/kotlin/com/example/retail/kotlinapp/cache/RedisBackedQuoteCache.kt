package com.example.retail.kotlinapp.cache

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RedisBackedQuoteCache(
    @Value("\${app.cache.redis-enabled:false}") private val redisEnabled: Boolean,
    @Value("\${app.cache.ttl-seconds:180}") ttlSeconds: Long,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val ttl = Duration.ofSeconds(ttlSeconds)
    private val fallbackCache = ConcurrentHashMap<String, QuoteComputation>()

    fun find(key: String): QuoteComputation? {
        if (redisEnabled) {
            try {
                val payload = redisTemplate.opsForValue().get(key)
                if (payload != null) {
                    meterRegistry.counter("pricing.cache.hit", "tier", "redis").increment()
                    return objectMapper.readValue(payload, QuoteComputation::class.java)
                }
            } catch (ex: Exception) {
                meterRegistry.counter("pricing.cache.fallback", "operation", "read").increment()
                log.warn("Redis read failed, falling back to local cache", ex)
            }
        }

        return fallbackCache[key]?.also {
            meterRegistry.counter("pricing.cache.hit", "tier", "local").increment()
        } ?: run {
            meterRegistry.counter("pricing.cache.miss").increment()
            null
        }
    }

    fun store(key: String, value: QuoteComputation) {
        fallbackCache[key] = value
        if (!redisEnabled) {
            return
        }

        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl)
        } catch (ex: Exception) {
            meterRegistry.counter("pricing.cache.fallback", "operation", "write").increment()
            log.warn("Redis write failed, retaining local cache only", ex)
        }
    }

    fun evictBySku(sku: String) {
        val prefix = "quote:${sku.trim().uppercase()}:"
        fallbackCache.keys.removeIf { it.startsWith(prefix) }

        if (!redisEnabled) {
            return
        }

        try {
            val keys = redisTemplate.keys("$prefix*")
            if (!keys.isNullOrEmpty()) {
                redisTemplate.delete(keys)
            }
        } catch (ex: Exception) {
            meterRegistry.counter("pricing.cache.fallback", "operation", "evict").increment()
            log.warn("Redis eviction failed for sku={}", sku, ex)
        }
    }
}

