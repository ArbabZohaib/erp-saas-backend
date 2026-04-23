package com.erp.modules.hr;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
public class LeaveRequestEntity extends BaseEntity {

    @Column(name = "employee_user_id", nullable = false)
    private UUID employeeUserId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LeaveStatus status = LeaveStatus.PENDING;
}
