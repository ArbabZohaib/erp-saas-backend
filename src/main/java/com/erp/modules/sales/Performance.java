package com.erp.modules.sales;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "performances")
@Getter
@Setter
public class Performance extends BaseEntity {

    @Column(name = "sales_officer_id", nullable = false)
    private UUID salesOfficerId;

    @Column(name = "total_sales", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal incentives = BigDecimal.ZERO;
}
