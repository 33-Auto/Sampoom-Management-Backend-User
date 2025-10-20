package com.sampoom.backend.user.common.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization");

        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("ACCESS_TOKEN");

        Server localServer = new Server()
                .url("http://localhost:8080/api/user")
                .description("로컬 서버");

        Server prodServer = new Server()
                .url("https://sampoom.store/api/user")
                .description("배포 서버");

        SecurityRequirement cookieAuthRequirement = new SecurityRequirement()
                .addList("cookieAuth");
        SecurityRequirement bearerAuthRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("삼삼오토 User Service API")
                        .description("User 서비스 REST API 문서")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuth)
                        .addSecuritySchemes("cookieAuth", cookieAuth)
                )
                .addSecurityItem(cookieAuthRequirement)
                .addSecurityItem(bearerAuthRequirement)
                .servers(List.of(localServer, prodServer));
    }
}
