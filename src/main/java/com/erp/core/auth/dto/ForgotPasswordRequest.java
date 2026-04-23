package com.erp.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ForgotPasswordRequest(
        @NotNull UUID orgId,
        @NotBlank @Email String email
) {
}
