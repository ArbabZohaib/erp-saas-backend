package com.erp.modules.hr.dto;

import com.erp.modules.hr.CompensationPlanType;
import com.erp.modules.hr.SalaryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryCalculationResponse(
        UUID salaryId,
        UUID employeeUserId,
        LocalDate periodMonth,
        BigDecimal baseAmount,
        BigDecimal targetAmount,
        BigDecimal achievedAmount,
        BigDecimal incentivePercent,
        BigDecimal incentiveAmount,
        BigDecimal totalAmount,
        boolean targetMet,
        SalaryStatus status,
        CompensationPlanType planType,
        String planSnapshotJson
) {
}
