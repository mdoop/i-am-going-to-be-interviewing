package com.example.retail.javaapp.controller;

import com.example.retail.contract.PriceQuoteRequest;
import com.example.retail.contract.PriceQuoteResponse;
import com.example.retail.javaapp.service.PriceQuoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/price-quotes")
public class PriceQuoteController {

    private final PriceQuoteService priceQuoteService;

    public PriceQuoteController(PriceQuoteService priceQuoteService) {
        this.priceQuoteService = priceQuoteService;
    }

    @PostMapping
    public PriceQuoteResponse quote(@Valid @RequestBody PriceQuoteRequest request) {
        return priceQuoteService.quote(request);
    }
}

