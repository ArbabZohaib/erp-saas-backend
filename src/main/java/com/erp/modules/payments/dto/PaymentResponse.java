package com.erp.modules.payments.dto;

import com.erp.modules.payments.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orgId,
        UUID orderId,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentMethod method,
        Instant createdAt,
        Instant updatedAt
) {
}
