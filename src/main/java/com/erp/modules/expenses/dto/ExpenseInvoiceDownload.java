package com.erp.modules.expenses.dto;

public record ExpenseInvoiceDownload(
        String fileName,
        String contentType,
        byte[] content
) {
}
