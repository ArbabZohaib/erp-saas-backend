package com.erp.modules.expenses.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseMonthlyComparisonRow(
        UUID userId,
        String userEmail,
        BigDecimal currentMonthAmount,
        BigDecimal previousMonthAmount,
        BigDecimal deltaAmount,
        BigDecimal deltaPercent,
        long currentMonthCount
) {
}
