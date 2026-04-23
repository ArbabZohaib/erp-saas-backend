package com.erp.core.users.dto;

import com.erp.core.users.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID orgId,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
