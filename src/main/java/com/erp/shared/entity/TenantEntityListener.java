package com.erp.shared.entity;

import com.erp.core.tenant.TenantContext;
import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Sets orgId from {@link TenantContext} when missing (e.g. new entities created in a web request).
 */
public class TenantEntityListener {

    @PrePersist
    public void onPrePersist(BaseEntity entity) {
        if (entity.getOrgId() == null) {
            UUID orgId = TenantContext.getOrgId();
            if (orgId != null) {
                entity.setOrgId(orgId);
            }
        }
    }
}
