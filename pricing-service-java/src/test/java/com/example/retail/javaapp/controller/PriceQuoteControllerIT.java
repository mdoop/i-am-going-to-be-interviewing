package com.example.retail.javaapp.controller;

import com.example.retail.contract.Channel;
import com.example.retail.contract.CustomerSegment;
import com.example.retail.contract.PromotionImportRequest;
import com.example.retail.contract.PromotionRule;
import com.example.retail.contract.PromotionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PriceQuoteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void servesApiPlaygroundAtRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Retail Pricing API Playground")));
    }

    @Test
    void returnsExpectedQuoteForHappyPath() throws Exception {
        mockMvc.perform(post("/v1/price-quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Trace-Id", "java-test-trace")
                        .content("""
                                {
                                  "sku": "SKU-RED-CHAIR",
                                  "storeId": "STORE-100",
                                  "channel": "ONLINE",
                                  "customerSegment": "LOYALTY",
                                  "quantity": 1,
                                  "couponCode": "SAVE15"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "java-test-trace"))
                .andExpect(jsonPath("$.basePrice").value(120.00))
                .andExpect(jsonPath("$.finalPrice").value(94.50))
                .andExpect(jsonPath("$.appliedPromotions", hasSize(2)))
                .andExpect(jsonPath("$.pricingSource").value("DATABASE"));
    }

    @Test
    void rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/v1/price-quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "",
                                  "storeId": "STORE-100",
                                  "channel": "ONLINE",
                                  "customerSegment": "LOYALTY",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importsPromotionsAndReadsThemBack() throws Exception {
        PromotionImportRequest request = new PromotionImportRequest(List.of(
                new PromotionRule(
                        "FLASH-LAMP-10",
                        "SKU-BLUE-LAMP",
                        "Flash lamp promo",
                        PromotionType.AMOUNT_OFF,
                        new BigDecimal("10.00"),
                        null,
                        1,
                        CustomerSegment.ALL,
                        Channel.ANY,
                        null,
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2030-01-01T00:00:00Z"),
                        95
                )
        ));

        mockMvc.perform(post("/v1/promotions/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1));

        mockMvc.perform(get("/v1/promotions/SKU-BLUE-LAMP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("FLASH-LAMP-10"));
    }
}
