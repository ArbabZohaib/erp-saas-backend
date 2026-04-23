package com.erp.modules.analytics.dto;

import java.math.BigDecimal;

public record SalesBucket(
        String period,
        BigDecimal totalSales,
        long orderCount
) {
}
