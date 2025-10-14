package com.sampoom.backend.user.common.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");

        return new OpenAPI()
                .info(new Info()
                        .title("삼품관리-User API")
                        .description("삼삼오토 REST API Document")
                        .version("1.0.0"))
                .addServersItem(server);
    }
}
