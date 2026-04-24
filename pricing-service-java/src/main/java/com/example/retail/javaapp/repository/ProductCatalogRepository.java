package com.example.retail.javaapp.repository;

import com.example.retail.contract.ProductPrice;

import java.util.Optional;

public interface ProductCatalogRepository {

    Optional<ProductPrice> findBySkuAndStoreId(String sku, String storeId);
}

