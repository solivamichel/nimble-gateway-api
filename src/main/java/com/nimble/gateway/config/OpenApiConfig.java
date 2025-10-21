package com.nimble.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String bearerName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Nimble Gateway API")
                        .description("API Nimble - Pagamentos & Cobranças")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(bearerName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(bearerName, new SecurityScheme()
                                .name(bearerName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
