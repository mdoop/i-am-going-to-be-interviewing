package com.example.retail.javaapp.service;

import com.example.retail.contract.PromotionImportRequest;
import com.example.retail.contract.PromotionImportResponse;
import com.example.retail.contract.PromotionRule;
import com.example.retail.javaapp.cache.RedisBackedQuoteCache;
import com.example.retail.javaapp.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionAdminService {

    private final PromotionRepository promotionRepository;
    private final RedisBackedQuoteCache quoteCache;

    public PromotionAdminService(PromotionRepository promotionRepository, RedisBackedQuoteCache quoteCache) {
        this.promotionRepository = promotionRepository;
        this.quoteCache = quoteCache;
    }

    @Transactional
    public PromotionImportResponse importPromotions(PromotionImportRequest request) {
        promotionRepository.replaceAll(request.promotions());
        List<String> refreshedSkus = request.promotions()
                .stream()
                .map(PromotionRule::sku)
                .distinct()
                .toList();
        refreshedSkus.forEach(quoteCache::evictBySku);
        return new PromotionImportResponse(request.promotions().size(), refreshedSkus);
    }

    public List<PromotionRule> findBySku(String sku) {
        return promotionRepository.findBySku(sku);
    }
}

