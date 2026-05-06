package com.erp.modules.orders.dto;

import com.erp.modules.orders.OrderFulfillmentStatus;
import jakarta.validation.constraints.NotNull;

public record OrderFulfillmentStatusRequest(
        @NotNull OrderFulfillmentStatus status
) {
}
