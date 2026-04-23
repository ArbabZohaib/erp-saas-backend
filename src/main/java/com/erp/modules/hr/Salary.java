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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "salaries")
@Getter
@Setter
public class Salary extends BaseEntity {

    @Column(name = "employee_user_id", nullable = false)
    private UUID employeeUserId;

    /** First day of salary month */
    @Column(nullable = false)
    private LocalDate periodMonth;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount = BigDecimal.ZERO;

    @Column(name = "achieved_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal achievedAmount = BigDecimal.ZERO;

    @Column(name = "incentive_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal incentiveAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SalaryStatus status = SalaryStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 32)
    private CompensationPlanType planType;

    @Column(name = "plan_snapshot_json", columnDefinition = "text")
    private String planSnapshotJson;
}
