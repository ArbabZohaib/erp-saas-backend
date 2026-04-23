package com.erp.modules.payments.dto;

import com.erp.modules.payments.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID orderId,
        @NotNull BigDecimal amount,
        @NotNull LocalDate paymentDate,
        @NotNull PaymentMethod method
) {
}
