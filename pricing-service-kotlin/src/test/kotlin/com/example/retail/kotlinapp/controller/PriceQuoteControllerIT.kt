package com.example.retail.kotlinapp.controller

import com.example.retail.contract.Channel
import com.example.retail.contract.CustomerSegment
import com.example.retail.contract.PromotionImportRequest
import com.example.retail.contract.PromotionRule
import com.example.retail.contract.PromotionType
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PriceQuoteControllerIT {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `serves api playground at root`() {
        val rootResult = mockMvc.get("/")
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        assertEquals("index.html", rootResult.response.forwardedUrl)

        mockMvc.get("/index.html")
            .andExpect {
                status { isOk() }
                content { string(containsString("Retail Pricing API Playground")) }
            }
    }

    @Test
    fun `returns expected quote for happy path`() {
        mockMvc.post("/v1/price-quotes") {
            contentType = MediaType.APPLICATION_JSON
            header("X-Trace-Id", "kotlin-test-trace")
            content =
                """
                {
                  "sku": "SKU-RED-CHAIR",
                  "storeId": "STORE-100",
                  "channel": "ONLINE",
                  "customerSegment": "LOYALTY",
                  "quantity": 1,
                  "couponCode": "SAVE15"
                }
                """.trimIndent()
        }.andExpect {
            status { isOk() }
            header { string("X-Trace-Id", "kotlin-test-trace") }
            jsonPath("$.basePrice") { value(120.00) }
            jsonPath("$.finalPrice") { value(94.50) }
            jsonPath("$.appliedPromotions.length()") { value(2) }
            jsonPath("$.pricingSource") { value("DATABASE") }
        }
    }

    @Test
    fun `rejects invalid request`() {
        mockMvc.post("/v1/price-quotes") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                  "sku": "",
                  "storeId": "STORE-100",
                  "channel": "ONLINE",
                  "customerSegment": "LOYALTY",
                  "quantity": 0
                }
                """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `imports promotions and reads them back`() {
        val request = PromotionImportRequest(
            listOf(
                PromotionRule(
                    "FLASH-LAMP-10",
                    "SKU-BLUE-LAMP",
                    "Flash lamp promo",
                    PromotionType.AMOUNT_OFF,
                    BigDecimal("10.00"),
                    null,
                    1,
                    CustomerSegment.ALL,
                    Channel.ANY,
                    null,
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2030-01-01T00:00:00Z"),
                    95,
                ),
            ),
        )

        mockMvc.post("/v1/promotions/import") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsBytes(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.importedCount") { value(1) }
        }

        mockMvc.get("/v1/promotions/SKU-BLUE-LAMP")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value("FLASH-LAMP-10") }
            }
    }
}
