package com.erp.modules.expenses;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Setter
public class Expense extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ExpenseApprovalStatus approvalStatus;

    @Column(name = "approval_note")
    private String approvalNote;

    @Column(name = "approved_by_user_id")
    private UUID approvedByUserId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "bill_extracted_amount", precision = 19, scale = 4)
    private BigDecimal billExtractedAmount;

    @Column(name = "bill_extraction_confidence")
    private String billExtractionConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "scrutiny_level", nullable = false)
    private ExpenseScrutinyLevel scrutinyLevel;
}
