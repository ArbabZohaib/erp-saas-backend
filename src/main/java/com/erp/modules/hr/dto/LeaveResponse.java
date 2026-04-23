package com.erp.modules.hr.dto;

import com.erp.modules.hr.LeaveStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveResponse(
        UUID id,
        UUID orgId,
        UUID employeeUserId,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
