package com.erp.modules.expenses.dto;

import java.math.BigDecimal;

public record ExpenseAmountExtractionResponse(
        BigDecimal detectedAmount,
        String confidence,
        String message
) {
}
