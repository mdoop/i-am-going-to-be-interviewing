package com.example.retail.javaapp.cache;

import com.example.retail.contract.AppliedPromotion;
import com.example.retail.contract.FulfillmentOption;
import com.example.retail.contract.FulfillmentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisBackedQuoteCacheTest {

    @SuppressWarnings("unchecked")
    @Test
    void fallsBackToLocalCacheWhenRedisFails() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doThrow(new RuntimeException("redis down"))
                .when(valueOperations)
                .set(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        Mockito.when(valueOperations.get("quote:SKU-RED-CHAIR:STORE-100:ONLINE:LOYALTY:1:SAVE15"))
                .thenThrow(new RuntimeException("redis down"));

        RedisBackedQuoteCache cache = new RedisBackedQuoteCache(
                true,
                180,
                redisTemplate,
                new ObjectMapper().findAndRegisterModules(),
                new SimpleMeterRegistry()
        );

        QuoteComputation computation = new QuoteComputation(
                new BigDecimal("120.00"),
                List.of(new AppliedPromotion("PROMO", "desc", com.example.retail.contract.PromotionType.AMOUNT_OFF, new BigDecimal("15.00"), 100)),
                new BigDecimal("105.00"),
                List.of(new FulfillmentOption(FulfillmentType.DELIVERY, 48))
        );

        String key = "quote:SKU-RED-CHAIR:STORE-100:ONLINE:LOYALTY:1:SAVE15";
        cache.store(key, computation);

        assertThat(cache.find(key)).contains(computation);
    }
}

