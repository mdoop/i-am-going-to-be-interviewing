package com.example.retail.contract;

public record FulfillmentOption(
        FulfillmentType type,
        int etaHours
) {
}

