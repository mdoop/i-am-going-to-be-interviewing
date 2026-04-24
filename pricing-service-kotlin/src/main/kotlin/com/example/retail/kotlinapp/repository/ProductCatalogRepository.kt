package com.example.retail.kotlinapp.repository

import com.example.retail.contract.ProductPrice

interface ProductCatalogRepository {
    fun findBySkuAndStoreId(sku: String, storeId: String): ProductPrice?
}

