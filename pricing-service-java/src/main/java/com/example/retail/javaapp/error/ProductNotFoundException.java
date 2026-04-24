package com.example.retail.javaapp.error;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String sku, String storeId) {
        super("No price found for sku=%s storeId=%s".formatted(sku, storeId));
    }
}

