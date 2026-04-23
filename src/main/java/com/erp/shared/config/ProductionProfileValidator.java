package com.erp.shared.config;

import com.erp.core.security.JwtDevDefaults;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Fails fast on insecure or incomplete configuration when {@code prod} is active.
 */
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionProfileValidator {

    private final Environment env;
    private final CorsAppProperties corsAppProperties;

    @PostConstruct
    void validate() {
        String secret = env.getProperty("app.jwt.secret", "");
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) must be set for profile 'prod'.");
        }
        if (JwtDevDefaults.INSECURE_PLACEHOLDER_SECRET.equals(secret.trim())) {
            throw new IllegalStateException(
                    "The sample JWT secret from application.yml must not be used with profile 'prod'. "
                            + "Set a strong JWT_SECRET (e.g. openssl rand -base64 64).");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters for profile 'prod'.");
        }
        if (!StringUtils.hasText(corsAppProperties.getAllowedOrigins())) {
            throw new IllegalStateException(
                    "app.cors.allowed-origins (CORS_ALLOWED_ORIGINS) must be set for profile 'prod' "
                            + "to your real frontend origin(s), comma-separated.");
        }
    }
}
