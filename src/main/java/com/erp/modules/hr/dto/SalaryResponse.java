package com.erp.modules.hr.dto;

import com.erp.modules.hr.CompensationPlanType;
import com.erp.modules.hr.SalaryStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryResponse(
        UUID id,
        UUID orgId,
        UUID employeeUserId,
        LocalDate periodMonth,
        BigDecimal amount,
        BigDecimal baseAmount,
        BigDecimal targetAmount,
        BigDecimal achievedAmount,
        BigDecimal incentiveAmount,
        SalaryStatus status,
        CompensationPlanType planType,
        String planSnapshotJson,
        Instant createdAt,
        Instant updatedAt
) {
}
