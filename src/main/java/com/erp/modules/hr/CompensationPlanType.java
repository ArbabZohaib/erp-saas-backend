package com.erp.modules.hr;

/**
 * How payroll is derived for an employee. Extend as new rules are added.
 */
public enum CompensationPlanType {
    /** Fixed monthly base only; no target/incentive. */
    FIXED_BASE,
    /** Base + incentive when sales target is met (percent of achieved). */
    TARGET_INCENTIVE,
    /**
     * One-off or corrected payout entered by an admin ({@link com.erp.modules.hr.Salary} rows only).
     * Never stored on {@link com.erp.modules.hr.CompensationPlan}.
     */
    MANUAL_ADJUSTMENT
}
