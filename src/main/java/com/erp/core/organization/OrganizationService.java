package com.erp.core.organization;

import com.erp.core.module.ModuleCode;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public Organization getById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organization", id));
    }

    @Transactional(readOnly = true)
    public Set<ModuleCode> getEnabledModules(UUID orgId) {
        return getById(orgId).getEnabledModules();
    }

    @Transactional(readOnly = true)
    public boolean isModuleEnabled(UUID orgId, ModuleCode module) {
        return getById(orgId).getEnabledModules().contains(module);
    }
}
