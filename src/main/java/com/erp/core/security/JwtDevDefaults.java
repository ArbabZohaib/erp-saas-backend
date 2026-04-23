package com.erp.core.security;

/**
 * Must match the dev-only default in {@code application.yml} ({@code app.jwt.secret}).
 * Used to reject that value when the {@code prod} profile is active.
 */
public final class JwtDevDefaults {

    /**
     * 64 hex chars; never use in production.
     */
    public static final String INSECURE_PLACEHOLDER_SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private JwtDevDefaults() {
    }
}
