package com.sch.hubzone.hub_manager_service.hub_manager_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hub Management Service API")
                        .version("1.0.0")
                        .description("API per la gestione degli hub e delle prenotazioni"));
    }
}
