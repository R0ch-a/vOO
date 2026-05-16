package com.voo.airline.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vooOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("vOO Airline API")
                .description("API REST do simulador da companhia aérea vOO — Spring Boot 3 + PostgreSQL")
                .version("1.0.0")
                .contact(new Contact()
                    .name("vOO Airways")
                    .email("dev@voo.airline")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local")
            ));
    }
}
