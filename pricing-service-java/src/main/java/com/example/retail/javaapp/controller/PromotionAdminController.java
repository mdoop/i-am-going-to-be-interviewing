package com.example.retail.javaapp.controller;

import com.example.retail.contract.PromotionImportRequest;
import com.example.retail.contract.PromotionImportResponse;
import com.example.retail.contract.PromotionRule;
import com.example.retail.javaapp.service.PromotionAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/promotions")
public class PromotionAdminController {

    private final PromotionAdminService promotionAdminService;

    public PromotionAdminController(PromotionAdminService promotionAdminService) {
        this.promotionAdminService = promotionAdminService;
    }

    @PostMapping("/import")
    public PromotionImportResponse importPromotions(@Valid @RequestBody PromotionImportRequest request) {
        return promotionAdminService.importPromotions(request);
    }

    @GetMapping("/{sku}")
    public List<PromotionRule> promotionsForSku(@PathVariable("sku") String sku) {
        return promotionAdminService.findBySku(sku);
    }
}
