package com.Grupo1.GestionHoteleria_Backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión Hotelera - Grupo 1")
                        .version("1.0")
                        .description("Documentación interactiva de los endpoints para el sistema de reserva de habitaciones e intranet administrativa.")
                        .contact(new Contact()
                                .name("Soporte Desarrollo")
                                .email("eloyguillencanales@gmail.com")));
    }
}