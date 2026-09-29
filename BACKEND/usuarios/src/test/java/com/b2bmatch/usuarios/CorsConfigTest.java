package com.b2bmatch.usuarios;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * CORS de un microservicio, con la configuracion por defecto.
 *
 * Antes de este bloque la lista de origenes estaba escrita en el codigo, asi que
 * cambiar para produccion obligaba a recompilar los seis servicios. Y con la
 * lista de siempre, que solo tenia los dos localhost del dev server, en
 * produccion no habria habido ni un origen valido: el frontend vive en otro
 * dominio y el navegador bloqueaba la respuesta entera.
 *
 * Se prueba con la cadena de filtros real y no solo con el bean, porque lo que
 * importa no es que el bean tenga los origins sino que el CorsFilter los conteste.
 */
@DisplayName("CORS de usuarios con la configuracion por defecto")
class CorsConfigTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("el preflight desde el dev server responde con su Access-Control-Allow-Origin")
    void preflightDesdeElDevServer() throws Exception {
        mvc().perform(options("/api/users")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("el 127.0.0.1 del dev server tambien, que no es el mismo origen")
    void preflightDesdeElIpDelDevServer() throws Exception {
        mvc().perform(options("/api/users")
                        .header("Origin", "http://127.0.0.1:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5173"));
    }

    @Test
    @DisplayName("un origen de fuera no recibe ninguna cabecera de CORS")
    void unOrigenDeFueraNoRecibeCabeceras() throws Exception {
        // Esto no es un 403: el navegador simplemente ve que no hay
        // Access-Control-Allow-Origin y bloquea la respuesta. Lo que importa es que
        // el servicio no se lo conceda.
        mvc().perform(options("/api/users")
                        .header("Origin", "https://sitio-malicioso.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("un GET normal desde fuera tampoco recibe cabeceras de CORS")
    void getDesdeFueraTampoco() throws Exception {
        mvc().perform(get("/api/users")
                        .header("Origin", "https://sitio-malicioso.example"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
