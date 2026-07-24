package com.b2bmatch.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI usuariosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("B2BMatch - Usuarios API")
                        .version("1.0.0")
                        .description("API REST del microservicio de Usuarios.\n\n"
                                + "Gestiona el registro de usuarios del sistema B2BMatch."))
                .addServersItem(new Server()
                        .url("http://localhost:8081")
                        .description("Servidor local de desarrollo"));
    }
}
