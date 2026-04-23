package com.erp.core.tenant;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> ORG_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setOrgId(UUID orgId) {
        ORG_ID.set(orgId);
    }

    public static UUID getOrgId() {
        return ORG_ID.get();
    }

    public static void clear() {
        ORG_ID.remove();
    }
}
