package com.erp.modules.hr.dto;

import com.erp.modules.hr.CompensationPlanType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CompensationPlanResponse(
        UUID id,
        UUID orgId,
        UUID employeeUserId,
        CompensationPlanType planType,
        BigDecimal baseSalary,
        BigDecimal targetAmount,
        BigDecimal incentivePercent,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
