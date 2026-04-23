package com.erp.modules.hr.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryRequest(
        @NotNull UUID employeeUserId,
        @NotNull LocalDate periodMonth,
        @NotNull BigDecimal amount,
        /** Optional audit note for manual payout entries. */
        @Size(max = 2000) String adjustmentNote
) {
}
