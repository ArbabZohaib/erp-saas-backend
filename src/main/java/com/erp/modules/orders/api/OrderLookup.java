package com.erp.modules.orders.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Facade for other modules (payments, reminders) — do not inject {@code OrderRepository} outside orders.
 */
public interface OrderLookup {

    OrderSummary getSummary(UUID orgId, UUID orderId);

    BigDecimal sumTotalAmountForCustomer(UUID orgId, UUID customerId);
}
