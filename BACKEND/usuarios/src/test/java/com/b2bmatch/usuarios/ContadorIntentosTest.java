package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.b2bmatch.usuarios.service.ContadorIntentos;

/**
 * El mecanismo del limite de intentos va aparte porque su parte dificil no se
 * puede probar por el login: la poda necesita miles de entradas y cada intento
 * de login real paga un BCrypt. Aqui el reloj es falso, asi que la caducidad se
 * prueba sin dormir quince minutos.
 */
class ContadorIntentosTest {

    private static final int MAX_POR_CUENTA = 5;
    private static final int MAX_POR_IP = 20;
    private static final Duration DURACION = Duration.ofMinutes(15);
    private static final int MAX_ENTRADAS = 10;

    private final RelojDePrueba reloj = new RelojDePrueba(Instant.parse("2026-01-01T10:00:00Z"));

    private ContadorIntentos contador() {
        return new ContadorIntentos(MAX_POR_CUENTA, MAX_POR_IP, DURACION, MAX_ENTRADAS, reloj);
    }

    @Test
    @DisplayName("bloquea la cuenta al llegar al maximo de intentos")
    void bloqueaAlLlegarAlMaximo() {
        ContadorIntentos c = contador();

        for (int i = 1; i < MAX_POR_CUENTA; i++) {
            c.falloDeCuenta("victima");
            assertThat(c.bloqueado("victima")).as("intento %d todavia no bloquea", i).isFalse();
        }
        c.falloDeCuenta("victima");
        assertThat(c.bloqueado("victima")).isTrue();
    }

    @Test
    @DisplayName("el bloqueo caduca solo")
    void elBloqueoCaducaSolo() {
        ContadorIntentos c = contador();
        for (int i = 0; i < MAX_POR_CUENTA; i++) {
            c.falloDeCuenta("victima");
        }
        assertThat(c.bloqueado("victima")).isTrue();

        reloj.avanzar(DURACION.minusSeconds(1));
        assertThat(c.bloqueado("victima")).as("un segundo antes de caducar sigue bloqueada").isTrue();

        reloj.avanzar(Duration.ofSeconds(2));
        assertThat(c.bloqueado("victima")).as("pasada la ventana ya no").isFalse();
    }

    @Test
    @DisplayName("el tope por IP es mas alto que el de cuenta")
    void elTopePorIpEsMasAltoQueElDeCuenta() {
        ContadorIntentos c = contador();

        // Cinco fallos contra una sola cuenta: salta el limite de cuenta.
        for (int i = 0; i < MAX_POR_CUENTA; i++) {
            c.falloDeCuenta("cuenta|203.0.113.1");
        }
        assertThat(c.bloqueado("cuenta|203.0.113.1")).isTrue();

        // Pero la IP no se bloquea todavia. Si compartieran tope, la primera
        // cuenta que alguien atacara cerraria la IP entera, y con una oficina
        // detras de una NAT eso es tumbar a todos los coworkers de golpe.
        assertThat(c.bloqueado("ip|203.0.113.1")).isFalse();
    }

    @Test
    @DisplayName("el acierto borra los dos contadores del par")
    void elAciertoBorraLosDosContadores() {
        ContadorIntentos c = contador();
        for (int i = 0; i < MAX_POR_CUENTA - 1; i++) {
            c.falloDeCuenta("cuenta|ip");
            c.falloDeIp("ip");
        }

        c.exito("cuenta|ip", "ip");

        assertThat(c.siguiendo("cuenta|ip")).isFalse();
        assertThat(c.siguiendo("ip")).isFalse();
    }

    @Test
    @DisplayName("desbordar el mapa no borra el progreso de los demas")
    void desbordarElMapaNoBorraElProgresoDeLosDemas() {
        ContadorIntentos c = contador();

        // El atacante va probando la victima mientras llena el mapa de ruido, que
        // es como se ve de verdad: las dos cosas a la vez.
        for (int i = 0; i < MAX_POR_CUENTA - 1; i++) {
            c.falloDeCuenta("victima");
            for (int j = 0; j < MAX_ENTRADAS; j++) {
                c.falloDeIp("ruido" + i + "-" + j);
                reloj.avanzar(Duration.ofSeconds(1));
            }
        }

        assertThat(c.entradas()).isLessThanOrEqualTo(MAX_ENTRADAS);
        // La version anterior, al pasarse del limite, borraba de golpe todo lo que
        // no estuviera bloqueado en ese instante, o sea todos los contadores a
        // medias. Con eso un inundador ponia a cero el progreso de todo el mundo
        // de golpe y el limite se esquivaba solo. Ahora la victima, que acaba de
        // fallar, conserva su contador y el quinto intento la bloquea igual.
        assertThat(c.siguiendo("victima")).isTrue();
        c.falloDeCuenta("victima");
        assertThat(c.bloqueado("victima")).isTrue();
    }

    @Test
    @DisplayName("la poda no se lleva por delante un bloqueo vivo")
    void laPodaNoEvaporaUnBloqueoVivo() {
        // Tope de cuenta a uno, para tener bloqueos vivos desde el principio.
        ContadorIntentos c = new ContadorIntentos(1, 100, DURACION, MAX_ENTRADAS, reloj);
        c.falloDeCuenta("bloqueada");
        assertThat(c.bloqueado("bloqueada")).isTrue();

        for (int i = 0; i < MAX_ENTRADAS * 3; i++) {
            c.falloDeIp("ruido" + i);
            reloj.avanzar(Duration.ofSeconds(1));
        }

        // Desbordar el mapa no puede servir para limpiar el bloqueo de una
        // victima: si lo hiciera, el propio mecanismo de anti-DoS seria la via
        // para esquivarlo.
        assertThat(c.entradas()).isLessThanOrEqualTo(MAX_ENTRADAS);
        assertThat(c.bloqueado("bloqueada")).isTrue();
        assertThat(c.siguiendo("bloqueada")).isTrue();
    }

    @Test
    @DisplayName("el hueco se recupera solo cuando lo que ocupa ya caducó")
    void elHuecoSeRecuperaSolo() {
        ContadorIntentos c = new ContadorIntentos(1, 100, DURACION, MAX_ENTRADAS, reloj);

        // Se llena el mapa con bloqueos vivos.
        for (int i = 0; i < MAX_ENTRADAS; i++) {
            c.falloDeCuenta("caducable" + i);
            reloj.avanzar(Duration.ofSeconds(1));
        }
        assertThat(c.entradas()).isEqualTo(MAX_ENTRADAS);

        // Lleno, y con una ventana entera sin tocarse, la entrada mas antigua ya
        // no sirve y su sitio se reutiliza. Sin esta recursion el mapa se
        // llenaria una vez y no volveria a admitir cuentas nuevas nunca.
        reloj.avanzar(DURACION.plusMinutes(1));
        c.falloDeCuenta("nueva");

        assertThat(c.siguiendo("nueva")).isTrue();
        assertThat(c.siguiendo("caducable0")).isFalse();
        assertThat(c.entradas()).isLessThanOrEqualTo(MAX_ENTRADAS);
    }

    @Test
    @DisplayName("el tope del mapa se respeta siempre")
    void elTopeDelMapaSeRespeta() {
        ContadorIntentos c = new ContadorIntentos(1, 100, DURACION, MAX_ENTRADAS, reloj);

        for (int i = 0; i < MAX_ENTRADAS * 5; i++) {
            c.falloDeCuenta("cuenta" + i);
            c.falloDeIp("ip" + i);
            reloj.avanzar(Duration.ofSeconds(1));
            assertThat(c.entradas()).isLessThanOrEqualTo(MAX_ENTRADAS);
        }
    }

    /** Reloj que se mueve a dedo, para probar la caducidad sin dormir. */
    private static final class RelojDePrueba extends Clock {
        private Instant instante;

        RelojDePrueba(Instant inicio) {
            this.instante = inicio;
        }

        void avanzar(Duration duracion) {
            instante = instante.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return instante;
        }

        @Override
        public String toString() {
            return "RelojDePrueba[" + LocalDateTime.ofInstant(instante, ZoneOffset.UTC) + "]";
        }
    }
}
