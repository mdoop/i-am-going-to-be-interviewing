package com.example.retail.kotlinapp.repository

import com.example.retail.contract.PromotionRule

interface PromotionRepository {
    fun findBySku(sku: String): List<PromotionRule>

    fun replaceAll(promotions: List<PromotionRule>)
}

