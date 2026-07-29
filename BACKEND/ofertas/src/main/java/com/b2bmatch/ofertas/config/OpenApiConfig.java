package com.b2bmatch.ofertas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI ofertasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("B2BMatch - Ofertas API")
                        .version("1.0.0")
                        .description("API REST del microservicio de Ofertas.\n\n"
                                + "Gestiona las ofertas de trabajo, postulaciones y cotizaciones del sistema B2BMatch."))
                .addServersItem(new Server()
                        .url("http://localhost:8084")
                        .description("Servidor local de desarrollo"));
    }
}
