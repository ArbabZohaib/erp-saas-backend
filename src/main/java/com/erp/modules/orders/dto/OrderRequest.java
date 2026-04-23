package com.erp.modules.orders.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderRequest(
        @NotNull UUID customerId,
        @NotNull LocalDate orderDate,
        @NotNull BigDecimal totalAmount
) {
}
