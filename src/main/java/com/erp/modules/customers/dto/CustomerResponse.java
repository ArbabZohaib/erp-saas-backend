package com.erp.modules.customers.dto;

import com.erp.modules.customers.CustomerType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID orgId,
        String name,
        CustomerType type,
        String phone,
        String whatsappNumber,
        String email,
        String address,
        BigDecimal creditLimit,
        Integer paymentTermsDays,
        UUID assignedSalesOfficerId,
        Instant createdAt,
        Instant updatedAt
) {
}
