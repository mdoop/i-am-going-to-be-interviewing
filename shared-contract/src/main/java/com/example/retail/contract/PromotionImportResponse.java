package com.example.retail.contract;

import java.util.List;

public record PromotionImportResponse(
        int importedCount,
        List<String> refreshedSkus
) {
}

