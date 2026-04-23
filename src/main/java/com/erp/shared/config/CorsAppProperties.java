package com.erp.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsAppProperties {

    /**
     * Comma-separated list of allowed browser origins (e.g. https://app.example.com,https://www.example.com).
     */
    private String allowedOrigins =
            "http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,http://127.0.0.1:3000";
}
