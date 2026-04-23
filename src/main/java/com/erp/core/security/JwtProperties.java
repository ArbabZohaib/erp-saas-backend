package com.erp.core.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * HS256 secret (min 256 bits for production; override via env).
     */
    private String secret;

    private long expirationMs = 86_400_000L;
}
