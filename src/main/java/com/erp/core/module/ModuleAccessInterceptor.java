package com.erp.core.module;

import com.erp.core.organization.OrganizationService;
import com.erp.core.tenant.TenantContext;
import com.erp.shared.exceptions.ForbiddenModuleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ModuleAccessInterceptor implements HandlerInterceptor {

    private final ModulePathMapping modulePathMapping;
    private final OrganizationService organizationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return modulePathMapping.resolveModule(request.getRequestURI())
                .map(module -> checkModule(module))
                .orElse(true);
    }

    private boolean checkModule(ModuleCode module) {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            return true;
        }
        if (!organizationService.isModuleEnabled(orgId, module)) {
            throw new ForbiddenModuleException(module);
        }
        return true;
    }
}
