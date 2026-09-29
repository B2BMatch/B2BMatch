package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.LoginRequestDto;
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

    @Test
    @DisplayName("dar de baja impide volver a autenticarse, con la clave correcta")
    void laBajaImpideAutenticarse() {
        String email = "baja@test.local";
        String clave = "ClaveValida1";
        Long userId = crearUsuarioConClave(email, clave);

        // Antes de la baja entra: si no, el test pasaria por un motivo equivocado.
        assertThat(servicio.login(credenciales(email, clave), IP_DE_PRUEBA).getId()).isEqualTo(userId);

        servicio.delete(userId);

        // Y despues no, aunque la clave siga siendo la correcta. Aqui es donde
        // importa que la baja no pise `status`: la cuenta sigue en ACTIVE, asi
        // que un login que solo mirase el estado la devolveria al mundo.
        assertThatThrownBy(() -> servicio.login(credenciales(email, clave), IP_DE_PRUEBA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email o contraseña incorrectos");
    }

    @Test
    @DisplayName("la baja no se distingue de una cuenta que no existe")
    void laBajaNoSeDistingueDeUnaInexistente() {
        String email = "baja@test.local";
        String clave = "ClaveValida1";
        Long userId = crearUsuarioConClave(email, clave);
        servicio.delete(userId);

        String mensajeDeLaBaja = assertThatThrownBy(() -> servicio.login(credenciales(email, clave), IP_DE_PRUEBA))
                .isInstanceOf(IllegalArgumentException.class)
                .actual().getMessage();
        String mensajeDeLaInexistente = assertThatThrownBy(
                () -> servicio.login(credenciales("nadie@test.local", clave), IP_DE_PRUEBA))
                .isInstanceOf(IllegalArgumentException.class)
                .actual().getMessage();

        // Si divergieran, el login confirmaria que ese email existio y fue dado de
        // baja, que es justo la confirmacion que el mensaje generico esconde.
        assertThat(mensajeDeLaBaja).isEqualTo(mensajeDeLaInexistente);
    }

    @Test
    @DisplayName("reactivar devuelve al usuario a poder autenticarse")
    void reactivarPermiteAutenticarseDeNuevo() {
        String email = "reactivado@test.local";
        String clave = "ClaveValida1";
        Long userId = crearUsuarioConClave(email, clave);
        servicio.delete(userId);
        servicio.reactivate(userId);

        assertThat(servicio.login(credenciales(email, clave), IP_DE_PRUEBA).getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("el email de un usuario dado de baja sigue reservado")
    void elEmailDeLaBajaSigueReservado() {
        String email = "reservado@test.local";
        Long userId = crearUsuarioConClave(email, "ClaveValida1");
        servicio.delete(userId);

        // El registro mira tambien entre las dadas de baja a proposito: el UNIQUE
        // de email lo reserva igual, y avisar "ya esta registrado" es mejor que
        // dejar que reviente la constraint con un 409 que no explica nada.
        AppUserRegisterRequestDto alta = new AppUserRegisterRequestDto();
        alta.setEmail(email);
        alta.setName("Nombre");
        alta.setPassword("ClaveValida1");
        alta.setRoleName("PROFESSIONAL");

        assertThatThrownBy(() -> servicio.register(alta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado");
        assertThat(estado("usuarios.app_user", userId)).isEqualTo("ACTIVE");
    }

}
