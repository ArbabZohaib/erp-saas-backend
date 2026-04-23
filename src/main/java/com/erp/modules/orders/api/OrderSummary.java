package com.erp.modules.orders.api;

import com.erp.modules.orders.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderSummary(
        UUID id,
        UUID customerId,
        BigDecimal totalAmount,
        OrderStatus status
) {
}
