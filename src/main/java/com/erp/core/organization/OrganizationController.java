package com.erp.core.organization;

import com.erp.core.organization.dto.EnabledModulesResponse;
import com.erp.core.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/modules")
    public EnabledModulesResponse getEnabledModules() {
        UUID orgId = requireOrg();
        return new EnabledModulesResponse(new ArrayList<>(organizationService.getEnabledModules(orgId)));
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
