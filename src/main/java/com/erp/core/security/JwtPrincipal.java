package com.erp.core.security;

import com.erp.core.users.Role;

import java.util.UUID;

public record JwtPrincipal(UUID userId, UUID orgId, Role role) {
}
