package com.erp.modules.payments.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentTotals {

    BigDecimal getTotalPaidForOrder(UUID orgId, UUID orderId);
}
