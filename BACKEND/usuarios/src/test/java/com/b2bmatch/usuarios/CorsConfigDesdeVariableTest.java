package com.b2bmatch.usuarios;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

/**
 * CORS de un microservicio cuando CORS_ALLOWED_ORIGINS viene del entorno.
 *
 * Es el caso que arregla el bloque. Sin esto, poner el frontend de produccion
 * en su dominio obligaba a recompilar los seis servicios, y no habria forma de
 * hacerlo sin tocar codigo.
 */
@SpringBootTest(properties = "CORS_ALLOWED_ORIGINS=https://app.ejemplo.cl,https://admin.ejemplo.cl")
@DisplayName("CORS de usuarios con CORS_ALLOWED_ORIGINS definida")
class CorsConfigDesdeVariableTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("el frontend de produccion entra por su dominio")
    void elFrontendDeProduccionEntra() throws Exception {
        mvc().perform(options("/api/users")
                        .header("Origin", "https://app.ejemplo.cl")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://app.ejemplo.cl"));
    }

    @Test
    @DisplayName("el segundo origen de la lista tambien")
    void elSegundoOrigenTambien() throws Exception {
        mvc().perform(options("/api/users")
                        .header("Origin", "https://admin.ejemplo.cl")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://admin.ejemplo.cl"));
    }

    @Test
    @DisplayName("un origen que no esta en la variable no recibe cabeceras")
    void unOrigenFueraDeLaVariableNo() throws Exception {
        mvc().perform(options("/api/users")
                        .header("Origin", "https://sitio-malicioso.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("con la variable definida el dev server deja de entrar")
    void elDevServerDejaDeEntrar() throws Exception {
        // La variable sustituye, no acumula. Si sumara, un despliegue con la lista
        // de produccion seguiria dejando entrar al dev server, que es justo lo que
        // no se quiere cuando esa maquina ya no existe.
        mvc().perform(options("/api/users")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("el GET corriente tambien trae la cabecera, no solo el preflight")
    void getTambienTraeLaCabecera() throws Exception {
        // El navegador no solo pide permiso con un preflight: sin esta cabecera en
        // la respuesta real, el bloqueo ocurre igual aunque el preflight pase.
        mvc().perform(get("/actuator/health")
                        .header("Origin", "https://app.ejemplo.cl"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://app.ejemplo.cl"));
    }
}
