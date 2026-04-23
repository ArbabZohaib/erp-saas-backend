package com.erp.modules.sales.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SalesOfficerRequest(
        @NotBlank String name,
        UUID userId,
        String territory
) {
}
