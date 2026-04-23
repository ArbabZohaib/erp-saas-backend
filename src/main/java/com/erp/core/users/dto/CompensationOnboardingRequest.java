package com.erp.core.users.dto;

import com.erp.modules.hr.CompensationPlanType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Optional payroll setup during user creation (only if org has {@code HR}).
 */
public record CompensationOnboardingRequest(
        @NotNull CompensationPlanType planType,
        @NotNull BigDecimal baseSalary,
        /** Required when plan is {@link CompensationPlanType#TARGET_INCENTIVE}. */
        BigDecimal targetAmount,
        /** Percent of achieved sales; required when plan is target-based. */
        BigDecimal incentivePercent
) {
}
