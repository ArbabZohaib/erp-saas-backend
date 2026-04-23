package com.erp.modules.sales;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sales_officers")
@Getter
@Setter
public class SalesOfficer extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id")
    private UUID userId;

    private String territory;
}
