package com.erp.modules.orders.dto;

import com.erp.modules.orders.OrderStatus;
import com.erp.modules.orders.OrderFulfillmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID orgId,
        UUID customerId,
        LocalDate orderDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        OrderStatus status,
        OrderFulfillmentStatus fulfillmentStatus,
        Instant deliveredAt,
        UUID deliveredByUserId,
        String deliveredByUserEmail,
        Instant closedAt,
        UUID closedByUserId,
        String closedByUserEmail,
        Instant createdAt,
        Instant updatedAt
) {
}
