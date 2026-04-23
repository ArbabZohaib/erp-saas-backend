package com.erp.modules.hr.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalaryFinalizeBatchSuccess(
        UUID employeeUserId,
        UUID salaryId,
        BigDecimal totalAmount
) {
}
