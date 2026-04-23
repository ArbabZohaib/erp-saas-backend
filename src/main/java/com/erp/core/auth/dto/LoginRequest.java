package com.erp.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LoginRequest(
        @NotNull UUID orgId,
        @Email String email,
        @NotNull String password
) {
}
