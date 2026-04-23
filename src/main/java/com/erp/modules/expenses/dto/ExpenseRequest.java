package com.erp.modules.expenses.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseRequest(
        @NotNull UUID userId,
        @NotNull BigDecimal amount,
        @NotBlank String category,
        String description,
        @NotNull LocalDate date
) {
}
