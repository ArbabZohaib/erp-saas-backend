package com.erp.modules.sales;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendances")
@Getter
@Setter
public class Attendance extends BaseEntity {

    @Column(name = "sales_officer_id", nullable = false)
    private UUID salesOfficerId;

    @Column(name = "check_in", nullable = false)
    private Instant checkIn;

    @Column(name = "check_out")
    private Instant checkOut;
}
