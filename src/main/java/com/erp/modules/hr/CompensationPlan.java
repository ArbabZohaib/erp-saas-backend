package com.erp.modules.hr;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "compensation_plans")
@Getter
@Setter
public class CompensationPlan extends BaseEntity {

    @Column(name = "employee_user_id", nullable = false)
    private UUID employeeUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 32)
    private CompensationPlanType planType = CompensationPlanType.TARGET_INCENTIVE;

    @Column(name = "base_salary", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount = BigDecimal.ZERO;

    /** Incentive percent applied to achieved sales when target is met, e.g. 3.5 means 3.5%. */
    @Column(name = "incentive_percent", nullable = false, precision = 7, scale = 4)
    private BigDecimal incentivePercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}
