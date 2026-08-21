package com.vn.smart_space.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("SmartSpace API")
                                                .description("API Documentation cho dự án SmartSpace")
                                                .version("1.0.0"))
                                // Add Authorized for swagger
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                                .components(new Components()
                                                .addSecuritySchemes("Bearer Token",
                                                                new SecurityScheme()
                                                                                .name("Authorization")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Nhập JWT access token (không cần ghi chữ 'Bearer')")));
        }
}
