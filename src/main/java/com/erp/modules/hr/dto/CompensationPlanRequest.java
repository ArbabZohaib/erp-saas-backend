package com.erp.modules.hr.dto;

import com.erp.modules.hr.CompensationPlanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CompensationPlanRequest(
        @NotNull UUID employeeUserId,
        @NotNull CompensationPlanType planType,
        @NotNull @DecimalMin("0.0") BigDecimal baseSalary,
        @NotNull @DecimalMin("0.0") BigDecimal targetAmount,
        @NotNull @DecimalMin("0.0") BigDecimal incentivePercent,
        boolean active
) {
}
