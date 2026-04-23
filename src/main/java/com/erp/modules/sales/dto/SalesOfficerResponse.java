package com.erp.modules.sales.dto;

import java.time.Instant;
import java.util.UUID;

public record SalesOfficerResponse(
        UUID id,
        UUID orgId,
        String name,
        UUID userId,
        String territory,
        Instant createdAt,
        Instant updatedAt
) {
}
