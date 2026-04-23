package com.erp.core.organization.dto;

import com.erp.core.module.ModuleCode;

import java.util.List;

public record EnabledModulesResponse(List<ModuleCode> modules) {
}
