package com.erp.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI erpOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP SaaS API")
                        .description("Multi-tenant ERP. Use **Authorize** and paste a JWT from `POST /api/v1/auth/login` (value: raw token only, without `Bearer `).")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT,
                                new SecurityScheme()
                                        .name(BEARER_JWT)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT returned by `/api/v1/auth/login`")));
    }
}
