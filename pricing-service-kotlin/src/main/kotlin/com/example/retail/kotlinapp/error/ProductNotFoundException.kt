package com.example.retail.kotlinapp.error

class ProductNotFoundException(sku: String, storeId: String) :
    RuntimeException("No price found for sku=$sku storeId=$storeId")

