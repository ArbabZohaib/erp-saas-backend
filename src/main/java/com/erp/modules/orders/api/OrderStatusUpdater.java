package com.erp.modules.orders.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderStatusUpdater {

    void applyPaidAmount(UUID orgId, UUID orderId, BigDecimal totalPaid);
}
