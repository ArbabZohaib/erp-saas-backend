package com.erp.modules.sales.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesByOfficerResponse(
        UUID officerId,
        String officerName,
        BigDecimal totalSales,
        long orderCount
) {
}
