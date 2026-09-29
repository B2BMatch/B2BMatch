package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.b2bmatch.usuarios.config.JwtService;
import com.b2bmatch.usuarios.config.JwtUtil;
import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

/**
 * La caducidad del token se define con JWT_EXPIRATION_MS.
 *
 * Estaba en el .env y en el .env.example, pero los application.yaml la traian
 * como un 86400000 escrito a mano y ninguna propiedad la leia: cambiar la
 * variable no hacia nada y no habia forma de enterarse. Este test falla si
 * alguien vuelve a dejar el numero en el yaml.
 *
 * Va contra el contexto real y no contra la clase suelta, porque el defecto era
 * justo de cableado: el valor se pierde entre el .env y el contenedor, no dentro
 * de JwtService.
 */
@SpringBootTest(properties = "JWT_EXPIRATION_MS=90000")
@ActiveProfiles("test")
class CaducidadDeTokenTest extends AbstractIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("el token caduca cuando dice JWT_EXPIRATION_MS y no cuando dice el yaml")
    void laCaducidadVieneDeLaVariableDeEntorno() {
        Date issuedAt = new Date();
        String token = jwtService.generateToken("caduca@test.local", "CUSTOMER", 4242L);

        Date expiration = jwtUtil.parseToken(token).getExpiration();

        long ventana = expiration.getTime() - issuedAt.getTime();
        // 90 000 ms es lo que dice la propiedad de arriba. El yaml pedia
        // 86 400 000, o sea un dia entero, asi que un test que aceptara "algo
        // cercano a un dia" no serviria para nada.
        assertThat(ventana).isBetween(Duration.ofSeconds(89).toMillis(), Duration.ofSeconds(90).toMillis());
    }
}
