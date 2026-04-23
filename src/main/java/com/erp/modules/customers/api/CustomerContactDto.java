package com.erp.modules.customers.api;

import java.util.UUID;

public record CustomerContactDto(
        UUID id,
        String name,
        String email,
        String phone,
        String whatsappNumber
) {
}
