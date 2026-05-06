package com.erp.modules.expenses.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID orgId,
        UUID userId,
        String userEmail,
        BigDecimal amount,
        String category,
        String description,
        LocalDate date,
        String approvalStatus,
        String approvalNote,
        UUID approvedByUserId,
        Instant approvedAt,
        BigDecimal billExtractedAmount,
        String billExtractionConfidence,
        String scrutinyLevel,
        Boolean invoiceAttached,
        Instant createdAt,
        Instant updatedAt
) {
}
