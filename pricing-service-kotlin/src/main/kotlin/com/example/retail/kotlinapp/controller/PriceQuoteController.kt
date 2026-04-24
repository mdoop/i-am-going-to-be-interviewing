package com.example.retail.kotlinapp.controller

import com.example.retail.contract.PriceQuoteRequest
import com.example.retail.contract.PriceQuoteResponse
import com.example.retail.kotlinapp.service.PriceQuoteService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/price-quotes")
class PriceQuoteController(
    private val priceQuoteService: PriceQuoteService,
) {
    @PostMapping
    fun quote(@Valid @RequestBody request: PriceQuoteRequest): PriceQuoteResponse = priceQuoteService.quote(request)
}

