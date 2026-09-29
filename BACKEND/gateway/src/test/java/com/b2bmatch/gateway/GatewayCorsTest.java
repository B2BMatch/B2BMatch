package com.b2bmatch.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.web.cors.CorsConfiguration;

/**
 * CORS del gateway por defecto, sin CORS_ALLOWED_ORIGINS en el entorno.
 *
 * El gateway es el unico punto de entrada en produccion (Caddy hace reverse_proxy
 * y no toca CORS), asi que de esta lista depende todo el trafico del navegador. Lo
 * que se fija aqui es que la lista existe y que no se hadilatado a un `*` que
 * dejaria pasar cualquier pagina.
 */
@SpringBootTest(properties = {
        "JWT_SECRET=clave-de-prueba-para-el-test-del-gateway-32-chars-minimo"
})
@DisplayName("CORS del gateway con la configuracion por defecto")
class GatewayCorsTest {

    @Autowired
    private GlobalCorsProperties corsProperties;

    @Test
    @DisplayName("por defecto solo se admiten los dos localhost del dev server")
    void porDefectoSoloLocalhost() {
        CorsConfiguration config = corsProperties.getCorsConfigurations().get("/**");

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "http://127.0.0.1:5173");
    }

    @Test
    @DisplayName("no se admiten credenciales desde un origen cualquiera")
    void noSeAbreElAsterisco() {
        CorsConfiguration config = corsProperties.getCorsConfigurations().get("/**");

        // allowCredentials(true) con allowedOrigins("*") es la combinacion que
        // permitiria a cualquier pagina del internet llamar a la API con cookies.
        // Spring ni la acepta, pero el test la vigila para que nadie la introduzca
        // cambiando allowedOrigins por allowedOriginPatterns.
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getAllowedOrigins()).doesNotContain("*");
        assertThat(config.getAllowedOriginPatterns()).isNull();
    }
}
