package com.example.retail.kotlinapp.controller

import com.example.retail.contract.PromotionImportRequest
import com.example.retail.contract.PromotionImportResponse
import com.example.retail.contract.PromotionRule
import com.example.retail.kotlinapp.service.PromotionAdminService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/promotions")
class PromotionAdminController(
    private val promotionAdminService: PromotionAdminService,
) {
    @PostMapping("/import")
    fun importPromotions(@Valid @RequestBody request: PromotionImportRequest): PromotionImportResponse =
        promotionAdminService.importPromotions(request)

    @GetMapping("/{sku}")
    fun promotionsForSku(@PathVariable("sku") sku: String): List<PromotionRule> = promotionAdminService.findBySku(sku)
}
