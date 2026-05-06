package com.erp.modules.expenses;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "expense_invoice_attachments")
@Getter
@Setter
public class ExpenseInvoiceAttachment extends BaseEntity {

    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;
}
