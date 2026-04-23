package com.erp.modules.customers;

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
@Table(name = "customers")
@Getter
@Setter
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CustomerType type;

    private String phone;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    private String email;

    private String address;

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Column(name = "payment_terms_days", nullable = false)
    private Integer paymentTermsDays = 0;

    @Column(name = "assigned_sales_officer_id")
    private UUID assignedSalesOfficerId;
}
