package com.erp.shared.exceptions;

import com.erp.core.module.ModuleCode;

public class ForbiddenModuleException extends BusinessException {

    public ForbiddenModuleException(ModuleCode module) {
        super("Module not enabled for tenant: " + module.name());
    }
}
