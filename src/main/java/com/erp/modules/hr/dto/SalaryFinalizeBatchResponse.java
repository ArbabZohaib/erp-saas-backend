package com.erp.modules.hr.dto;

import java.time.LocalDate;
import java.util.List;

public record SalaryFinalizeBatchResponse(
        LocalDate periodMonth,
        int successCount,
        int failureCount,
        List<SalaryFinalizeBatchSuccess> successes,
        List<SalaryFinalizeBatchFailure> failures
) {
}
