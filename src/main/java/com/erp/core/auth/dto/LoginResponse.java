package com.erp.core.auth.dto;

import com.erp.core.users.Role;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        UUID orgId,
        Role role
) {
}
