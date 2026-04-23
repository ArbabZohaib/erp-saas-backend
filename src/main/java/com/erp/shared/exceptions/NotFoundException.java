package com.erp.shared.exceptions;

import java.util.UUID;

public class NotFoundException extends BusinessException {

    public NotFoundException(String entity, UUID id) {
        super("%s not found: %s".formatted(entity, id));
    }

    public NotFoundException(String message) {
        super(message);
    }
}
