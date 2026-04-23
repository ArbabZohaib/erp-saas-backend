package com.erp.modules.hr.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestDto(
        @NotNull UUID employeeUserId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {
}
