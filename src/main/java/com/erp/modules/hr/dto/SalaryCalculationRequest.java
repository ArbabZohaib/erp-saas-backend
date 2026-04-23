package com.erp.modules.hr.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SalaryCalculationRequest(
        @NotNull UUID employeeUserId,
        /** Any date in target month; service normalizes to first day of month. */
        @NotNull LocalDate periodMonth,
        /**
         * When true, a finalized row for this employee/month is overwritten by a new calculation.
         * When false or omitted, finalize fails if the period is already finalized.
         */
        Boolean replaceFinalized
) {
}
