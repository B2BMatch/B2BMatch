package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.usuarios.service.AppUserService;
import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

/**
 * Baja de usuario y su cascada.
 *
 * usuarios es el unico servicio que escribe en datos de otros (es dueno de la
 * cascada), asi que es donde un error de `deleted_at` se propaga a 5 schemas.
 * Estos tests fijan tres reglas:
 *
 * <ol>
 * <li>dar de baja marca `deleted_at` y no reescribe `status` en ninguna tabla;
 * <li>la cascada reversible cubre las 6 tablas de datos y NO toca el catalogo
 * global (category/skill), que no es de nadie;
 * <li>notificaciones y resenias se borran en duro, porque no se restauran.</li>
 * </ol>
 */
@DisplayName("Baja de usuario y cascada")
class UsuarioBajaTest extends AbstractIntegrationTest {

    @Autowired
    private AppUserService servicio;

    @Test
    @DisplayName("dar de baja al usuario no toca su estado de negocio")
    void borrarNoTocaElEstado() {
        servicio.delete(usuario);

        assertThat(dadasDeBaja("usuarios.app_user")).isEqualTo(1);
        assertThat(estado("usuarios.app_user", usuario)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("la cascada da de baja perfiles y ofertas del usuario")
    void laCascadaDaDeBajaLoDelUsuario() {
        crearOferta(usuario);
        Long perfil = jdbc.queryForObject(
                "SELECT id FROM perfiles.customer_profile WHERE user_id = ?", Long.class, usuario);

        servicio.delete(usuario);

        assertThat(dadasDeBaja("perfiles.customer_profile")).isEqualTo(1);
        assertThat(dadasDeBaja("ofertas.job_offer")).isEqualTo(1);
        // Y la cascada no pisa el estado de negocio de lo que marca.
        assertThat(estado("perfiles.customer_profile", perfil)).isEqualTo("ACTIVE");
        assertThat(estado("ofertas.job_offer", 1L)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("la cascada NO toca las filas de otro usuario")
    void laCascadaRespetaALosDemas() {
        Long otro = jdbc.queryForObject(
                "INSERT INTO usuarios.app_user(role_id, email, password_hash) "
                        + "SELECT role_id, 'otro@test.local', 'x' FROM usuarios.app_user WHERE id = ? RETURNING id",
                Long.class, usuario);
        // Una oferta de cada uno: si la cascada se pasara de user_id, marcaria
        // las dos y el contador de "vivas" lo delataria.
        crearOferta(usuario);
        crearOferta(otro);

        servicio.delete(usuario);

        assertThat(dadasDeBaja("ofertas.job_offer")).isEqualTo(1);
        assertThat(vivas("ofertas.job_offer")).isEqualTo(1);
    }

    @Test
    @DisplayName("la cascada resuelve los servicios por subconsulta a los perfiles")
    void laCascadaAlcanzaLosServicios() {
        Long perfilProf = jdbc.queryForObject(
                "SELECT id FROM perfiles.professional_profile WHERE user_id = ?", Long.class, profesional);
        Long perfilEmp = jdbc.queryForObject(
                "SELECT id FROM perfiles.company_profile WHERE user_id = ?", Long.class, empresa);
        crearServicioProfesional(perfilProf);
        crearServicioEmpresa(perfilEmp);

        servicio.delete(profesional);
        servicio.delete(empresa);

        assertThat(dadasDeBaja("catalogo.professional_service")).isEqualTo(1);
        assertThat(dadasDeBaja("catalogo.company_service")).isEqualTo(1);
        // Un servicio de empresa no se marca al borrar a un profesional, ni al
        // reves: la subconsulta se resuelve por user_id, no "a todo".
        assertThat(dadasDeBaja("catalogo.professional_service")).isEqualTo(1);
    }

    @Test
    @DisplayName("notificaciones y resenias se borran en duro, no se restauran")
    void notificacionesYReseniasSeBoranEnDuro() {
        crearNotificacion(usuario);
        crearResenia(usuario);

        servicio.delete(usuario);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM notificaciones.notification", Long.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM resenias.review", Long.class)).isZero();
    }

    @Test
    @DisplayName("reactivar levanta la baja del usuario y de toda su cascada")
    void reactivarLevantaTodaLaCascada() {
        crearOferta(usuario);
        Long perfil = jdbc.queryForObject(
                "SELECT id FROM perfiles.customer_profile WHERE user_id = ?", Long.class, usuario);
        servicio.delete(usuario);
        assertThat(dadasDeBaja("perfiles.customer_profile")).isEqualTo(1);

        servicio.reactivate(usuario);

        assertThat(dadasDeBaja("usuarios.app_user")).isZero();
        assertThat(dadasDeBaja("perfiles.customer_profile")).isZero();
        assertThat(dadasDeBaja("ofertas.job_offer")).isZero();
        assertThat(estado("usuarios.app_user", usuario)).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject(
                "SELECT deleted_at IS NULL FROM perfiles.customer_profile WHERE id = ?", Boolean.class, perfil))
                .isTrue();
    }

    @Test
    @DisplayName("borrar dos veces o reactivar lo que esta vivo es un error")
    void operacionesInvalidas() {
        servicio.delete(usuario);

        assertThatThrownBy(() -> servicio.delete(usuario)).isInstanceOf(IllegalArgumentException.class);
        servicio.reactivate(usuario);
        assertThatThrownBy(() -> servicio.reactivate(usuario)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("borrar un usuario no toca el catalogo global (category/skill)")
    void borrarNoTocaElCatalogoGlobal() {
        // category y skill son taxonomia global: borrarle la cuenta a un
        // profesional no debe tocar lo que otros ven. El fixture de usuarios
        // ni siquiera las declara, y por eso este test tambien documenta que
        // el cascade no las necesita: si aparecieran en la cascada, el propio
        // test reventaria con 'relation does not exist'.
        servicio.delete(usuario);

        assertThat(dadasDeBaja("usuarios.app_user")).isEqualTo(1);
    }
}
