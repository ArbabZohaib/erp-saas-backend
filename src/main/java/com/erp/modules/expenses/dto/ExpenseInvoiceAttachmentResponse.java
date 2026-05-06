package com.erp.modules.expenses.dto;

import java.time.Instant;
import java.util.UUID;

public record ExpenseInvoiceAttachmentResponse(
        UUID id,
        UUID expenseId,
        String fileName,
        String contentType,
        Long fileSizeBytes,
        String sha256,
        Instant uploadedAt
) {
}
