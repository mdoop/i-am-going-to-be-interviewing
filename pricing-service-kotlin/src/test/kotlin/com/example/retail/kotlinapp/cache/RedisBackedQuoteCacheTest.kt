package com.example.retail.kotlinapp.cache

import com.example.retail.contract.AppliedPromotion
import com.example.retail.contract.FulfillmentOption
import com.example.retail.contract.FulfillmentType
import com.example.retail.contract.PromotionType
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.math.BigDecimal
import java.time.Duration

class RedisBackedQuoteCacheTest {
    @Test
    fun `falls back to local cache when redis fails`() {
        val redisTemplate = Mockito.mock(StringRedisTemplate::class.java)
        @Suppress("UNCHECKED_CAST")
        val valueOperations = Mockito.mock(ValueOperations::class.java) as ValueOperations<String, String>
        Mockito.`when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        Mockito.doThrow(RuntimeException("redis down"))
            .`when`(valueOperations)
            .set(Mockito.anyString(), Mockito.anyString(), Mockito.any(Duration::class.java))
        Mockito.`when`(valueOperations.get("quote:SKU-RED-CHAIR:STORE-100:ONLINE:LOYALTY:1:SAVE15"))
            .thenThrow(RuntimeException("redis down"))

        val cache = RedisBackedQuoteCache(
            true,
            180,
            redisTemplate,
            ObjectMapper().findAndRegisterModules(),
            SimpleMeterRegistry(),
        )

        val computation = QuoteComputation(
            basePrice = BigDecimal("120.00"),
            appliedPromotions = listOf(AppliedPromotion("PROMO", "desc", PromotionType.AMOUNT_OFF, BigDecimal("15.00"), 100)),
            finalPrice = BigDecimal("105.00"),
            fulfillmentOptions = listOf(FulfillmentOption(FulfillmentType.DELIVERY, 48)),
        )

        val key = "quote:SKU-RED-CHAIR:STORE-100:ONLINE:LOYALTY:1:SAVE15"
        cache.store(key, computation)

        assertThat(cache.find(key)).isEqualTo(computation)
    }
}
