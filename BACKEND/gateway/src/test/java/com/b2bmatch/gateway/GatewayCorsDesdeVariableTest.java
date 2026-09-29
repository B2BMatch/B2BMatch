package com.b2bmatch.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.web.cors.CorsConfiguration;

/**
 * CORS del gateway cuando CORS_ALLOWED_ORIGINS viene definida.
 *
 * Este es el caso que arregla el bloque: en produccion el frontend se sirve desde
 * Cloudflare Pages y la API desde otro dominio, asi que el navegador manda un
 * Origin que no estaba en ninguna lista y bloqueaba la respuesta entera. Sin esto,
 * cambiar los origenes obligaba a recompilar el gateway.
 */
@SpringBootTest(properties = {
        "JWT_SECRET=clave-de-prueba-para-el-test-del-gateway-32-chars-minimo",
        "CORS_ALLOWED_ORIGINS=https://app.ejemplo.cl,https://admin.ejemplo.cl"
})
@DisplayName("CORS del gateway con CORS_ALLOWED_ORIGINS definida")
class GatewayCorsDesdeVariableTest {

    @Autowired
    private GlobalCorsProperties corsProperties;

    @Test
    @DisplayName("la lista separada por comas se convierte en la lista de origenes")
    void laListaPorComasSeConvierteEnOrigenes() {
        CorsConfiguration config = corsProperties.getCorsConfigurations().get("/**");

        // La duda real de este bloque era esta: dentro del mapa cors-configurations,
        // un escalar con comas se convierte en lista, o se queda como un solo
        // origen mal formado que no casa con nada. Este test es la respuesta.
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins())
                .containsExactly("https://app.ejemplo.cl", "https://admin.ejemplo.cl");
    }

    @Test
    @DisplayName("los localhost dejan de estar permitidos cuando se define la variable")
    void losLocalhostDesaparecen() {
        CorsConfiguration config = corsProperties.getCorsConfigurations().get("/**");

        // La variable sustituye, no añade. Si se acumularan, un despliegue con la
        // lista de produccion seguiría dejando entrar al dev server.
        assertThat(config.getAllowedOrigins())
                .doesNotContain("http://localhost:5173", "http://127.0.0.1:5173");
    }
}
