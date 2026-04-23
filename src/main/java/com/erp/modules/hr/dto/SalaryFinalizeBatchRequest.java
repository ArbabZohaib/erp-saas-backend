package com.erp.modules.hr.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Finalize calculated payroll for many employees in one request. Each employee is processed in its own
 * transaction so partial success is possible.
 *
 * @param periodMonth      target month (any day in month; normalized to first day)
 * @param employeeUserIds  if null or empty, every <strong>active</strong> compensation plan in the org is included
 * @param replaceFinalized same meaning as {@link SalaryCalculationRequest#replaceFinalized()}
 */
public record SalaryFinalizeBatchRequest(
        @NotNull LocalDate periodMonth,
        List<UUID> employeeUserIds,
        Boolean replaceFinalized
) {
}
