package com.example.retail.javaapp.repository;

import com.example.retail.contract.PromotionRule;

import java.util.List;

public interface PromotionRepository {

    List<PromotionRule> findBySku(String sku);

    void replaceAll(List<PromotionRule> promotions);
}

