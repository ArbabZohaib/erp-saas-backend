package com.erp.modules.hr.dto;

import java.util.UUID;

public record SalaryFinalizeBatchFailure(
        UUID employeeUserId,
        String message
) {
}
