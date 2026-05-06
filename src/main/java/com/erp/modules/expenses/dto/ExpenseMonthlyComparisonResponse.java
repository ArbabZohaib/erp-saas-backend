package com.erp.modules.expenses.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseMonthlyComparisonResponse(
        String month,
        String previousMonth,
        String userId,
        BigDecimal currentMonthTotal,
        BigDecimal previousMonthTotal,
        BigDecimal deltaAmount,
        BigDecimal deltaPercent,
        long currentMonthCount,
        List<ExpenseMonthlyComparisonRow> rows
) {
}
