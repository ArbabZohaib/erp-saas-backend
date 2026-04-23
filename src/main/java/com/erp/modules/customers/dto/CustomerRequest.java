package com.erp.modules.customers.dto;

import com.erp.modules.customers.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerRequest(
        @NotBlank String name,
        @NotNull CustomerType type,
        String phone,
        String whatsappNumber,
        String email,
        String address,
        BigDecimal creditLimit,
        @NotNull Integer paymentTermsDays,
        UUID assignedSalesOfficerId
) {
}
