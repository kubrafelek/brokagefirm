package com.kubrafelek.brokagefirm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Brokage Firm API")
                        .description("Stock order management system API for managing customers, assets, and trading orders")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kubra Felek")
                                .email("kubrafelek@brokerfirm.com")))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Basic authentication using username and password headers")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
