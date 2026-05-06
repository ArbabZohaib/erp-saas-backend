package com.erp.modules.orders;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "SalesOrder")
@Table(name = "orders")
@Getter
@Setter
public class Order extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false, length = 32)
    private OrderFulfillmentStatus fulfillmentStatus = OrderFulfillmentStatus.INITIATED;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivered_by_user_id")
    private UUID deliveredByUserId;

    @Column(name = "closed_by_user_id")
    private UUID closedByUserId;
}
