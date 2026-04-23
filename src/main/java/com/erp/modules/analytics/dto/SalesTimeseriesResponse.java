package com.erp.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalesTimeseriesResponse(
        LocalDate from,
        LocalDate to,
        String groupBy,
        UUID customerId,
        UUID salesOfficerId,
        List<SalesBucket> buckets,
        BigDecimal totalSales,
        long totalOrders
) {
}
