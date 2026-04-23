package com.erp.shared.config;

import com.erp.core.security.JwtProperties;
import com.erp.core.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class, CorsAppProperties.class})
public class PropertiesConfig {
}
