package com.erp.modules.orders.api;

import java.time.LocalDate;
import java.util.UUID;

public record OrderReminderView(
        UUID id,
        UUID orgId,
        UUID customerId,
        LocalDate dueDate
) {
}
