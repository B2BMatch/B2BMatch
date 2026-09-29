package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

/**
 * El login no debe distinguir entre cuentas que existen y cuentas que no.
 *
 * Antes de este bloque, tres fallos distintos y tres textos distintos:
 * inexistente, suspendida y bloqueada por intentos. Con solo eso el endpoint
 * servia para enumerar quien tiene cuenta, y el de bloqueo era el mas comodo
 * porque no dependia del estado: cinco intentos y un sexto ya cambiaban la
 * respuesta.
 */
class LoginNoDivulgacionTest extends AbstractIntegrationTest {

    private static final String CLAVE = "ClaveValida1";
    private static final String GENERICO = "Email o contraseña incorrectos";
    private static final int INTENTOS_POR_IP = 20;

    @Test
    @DisplayName("todos los fallos de login devuelven el mismo mensaje")
    void todosLosFallosDicenLoMismo() {
        String suspendida = "suspendida@test.local";
        String inactiva = "inactiva@test.local";
        String borrada = "borrada@test.local";
        String activa = "activa@test.local";

        appUserService.updateStatus(crearUsuarioConClave(suspendida, CLAVE), "SUSPENDED");
        appUserService.updateStatus(crearUsuarioConClave(inactiva, CLAVE), "INACTIVE");
        appUserService.delete(crearUsuarioConClave(borrada, CLAVE));
        crearUsuarioConClave(activa, CLAVE);

        // Una IP distinta por caso, para que ninguno se frene a si mismo por el
        // contador y todos los mensajes sean los de "credencial incorrecta".
        assertThat(mensajeDeLoginFallido("nadie@test.local", CLAVE, "198.51.100.1"))
                .isEqualTo(GENERICO);
        assertThat(mensajeDeLoginFallido(activa, "ClaveEquivocada1", "198.51.100.2"))
                .isEqualTo(GENERICO);
        assertThat(mensajeDeLoginFallido(suspendida, CLAVE, "198.51.100.3"))
                .isEqualTo(GENERICO);
        assertThat(mensajeDeLoginFallido(inactiva, CLAVE, "198.51.100.4"))
                .isEqualTo(GENERICO);
        assertThat(mensajeDeLoginFallido(borrada, CLAVE, "198.51.100.5"))
                .isEqualTo(GENERICO);
    }

    @Test
    @DisplayName("el bloqueo por intentos tampoco revela si el email existe")
    void elBloqueoNoDistingueEntreQueExisteYQueNo() {
        String email = "existe@test.local";
        crearUsuarioConClave(email, CLAVE);

        // Cinco intentos con clave mala contra un email registrado, y el sexto,
        // que es el que destapaba la cuenta.
        for (int i = 0; i < 5; i++) {
            assertThat(mensajeDeLoginFallido(email, "ClaveEquivocada1", "198.51.100.20")).isEqualTo(GENERICO);
        }
        assertThat(mensajeDeLoginFallido(email, "ClaveEquivocada1", "198.51.100.20")).isEqualTo(GENERICO);

        // Misma cadencia contra un email que no esta registrado. Si aqui la
        // respuesta siguiera siendo la generica y antes no lo era, el atacante
        // tenia su orculo con seis peticiones y nada mas.
        for (int i = 0; i < 8; i++) {
            assertThat(mensajeDeLoginFallido("nadie@test.local", "ClaveEquivocada1", "198.51.100.21"))
                    .isEqualTo(GENERICO);
        }
    }

    @Test
    @DisplayName("bloquear una cuenta desde una IP no la bloquea desde las demas")
    void bloquearDesdeUnaIpNoAfectaALasDemas() {
        String email = "victima@test.local";
        Long id = crearUsuarioConClave(email, CLAVE);

        for (int i = 0; i < 6; i++) {
            mensajeDeLoginFallido(email, "ClaveEquivocada1", "198.51.100.30");
        }

        // El atacante ya no puede seguir, pero la victima entra bien desde su
        // casa. Con el contador por email solo esto fallaba: cinco intentos
        // ajenos dejaban a la victima fuera quince minutos, o sea que el
        // endpoint servia para tumbar cuentas, no solo para adivinarlas.
        assertThat(login(email, CLAVE, "198.51.100.31").getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("el limite por IP frena la enumeracion desde una sola maquina")
    void elLimitePorIpFrenaLaEnumeracion() {
        // Un unico hash para veinte cuentas: codificar veinte BCrypt en el
        // setup costaria mas que el test entero.
        String hash = passwordEncoder.encode(CLAVE);
        for (int i = 1; i <= INTENTOS_POR_IP; i++) {
            jdbc.update("INSERT INTO usuarios.app_user(role_id, email, password_hash) VALUES (?, ?, ?)",
                    rol, "objetivo" + i + "@test.local", hash);
        }

        String ip = "198.51.100.50";
        for (int i = 1; i <= INTENTOS_POR_IP; i++) {
            assertThat(mensajeDeLoginFallido("objetivo" + i + "@test.local", "ClaveEquivocada1", ip))
                    .isEqualTo(GENERICO);
        }

        // El agregado por IP ya esta al tope y pesa mas que el acierto. Sin el,
        // una maquina podria recorrer el padron probando una clave comun en cada
        // cuenta y, por lo que fueran fallando, quedarse con la lista de quien
        // esta registrado. El limite por cuenta no ayuda aqui, porque cada
        // objetivo se intenta una sola vez.
        assertThatThrownBy(() -> login("objetivo1@test.local", CLAVE, ip))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(GENERICO);
    }

    @Test
    @DisplayName("un acierto reinicia el presupuesto de intentos de esa IP")
    void elAciertoReiniciaElPresupuesto() {
        String email = "olvidadiza@test.local";
        crearUsuarioConClave(email, CLAVE);
        String ip = "198.51.100.40";

        // Cuatro fallos: uno menos de los que bloquean la cuenta.
        for (int i = 0; i < 4; i++) {
            mensajeDeLoginFallido(email, "ClaveEquivocada1", ip);
        }
        assertThat(login(email, CLAVE, ip).getEmail()).isEqualTo(email);

        // El acierto tiene que haber borrado el contador, porque si no, estos
        // cuatro fallos mas cruzarian el limite y el siguiente intento bloquearia
        // la cuenta. En una oficina entera detras de una NAT eso deja a la gente
        // fuera por dos tecleos mal pulsados.
        for (int i = 0; i < 4; i++) {
            assertThat(mensajeDeLoginFallido(email, "ClaveEquivocada1", ip)).isEqualTo(GENERICO);
        }
        assertThat(login(email, CLAVE, ip).getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("sin IP el login funciona, pero el limite sigue contando")
    void sinIpElLoginSigueFuncionando() {
        String email = "sinip@test.local";
        Long id = crearUsuarioConClave(email, CLAVE);

        // El servicio puede recibir un origen vacio si se invoca sin peticion, que
        // es lo que hace la propia suite. Se agrupa bajo "desconocida" para que el
        // limite siga contando en vez de quedarse sin contabilizar, que seria
        // dejar una puerta trasera al limite.
        assertThat(login(email, CLAVE, null).getId()).isEqualTo(id);
        assertThat(login(email, CLAVE, "   ").getId()).isEqualTo(id);
    }
}
