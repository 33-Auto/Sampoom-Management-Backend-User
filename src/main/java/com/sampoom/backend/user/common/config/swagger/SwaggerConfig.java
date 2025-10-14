package com.sampoom.backend.user.common.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        Server localServer = new Server()
                .url("http://localhost:8080/api/user")
                .description("로컬 서버");

        Server prodServer = new Server()
                .url("https://sampoom.store/api/user")
                .description("배포 서버");

        return new OpenAPI()
                .info(new Info()
                        .title("삼삼오토 User Service API")
                        .description("User 서비스 REST API 문서")
                        .version("1.0.0"))
                .servers(List.of(localServer, prodServer));
    }
}
