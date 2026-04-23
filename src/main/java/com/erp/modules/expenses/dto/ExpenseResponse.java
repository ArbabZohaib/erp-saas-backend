package com.erp.modules.expenses.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID orgId,
        UUID userId,
        BigDecimal amount,
        String category,
        String description,
        LocalDate date,
        Instant createdAt,
        Instant updatedAt
) {
}
