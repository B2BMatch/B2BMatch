package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.service.AppUserService;
import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

/**
 * Que rol se puede elegir al registrarse.
 *
 * El registro es publico, asi que la lista de roles que se aceptan es una linea de
 * defensa, no una validacion de formulario. Lo que importa es que un rol nuevo no
 * sea registrable por aparecer en la base de datos: por eso el test clave crea un
 * rol privilegiado que el codigo no conoce de nada y verifica que se rechaza.
 */
@DisplayName("Registro y eleccion de rol")
class RegistroRolTest extends AbstractIntegrationTest {

    @Autowired
    private AppUserService servicio;

    @ParameterizedTest
    @ValueSource(strings = { "CUSTOMER", "PROFESSIONAL", "COMPANY" })
    @DisplayName("los tres roles de autoservicio se pueden registrar")
    void losRolesDeAutoservicioSePuedenRegistrar(String roleName) {
        assertThat(servicio.register(alta(roleName)).getRoleName()).isEqualTo(roleName);
    }

    @Test
    @DisplayName("ADMIN no se puede elegir en el registro")
    void adminNoSePuedeElegir() {
        assertThatThrownBy(() -> servicio.register(alta("ADMIN")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("un rol privilegiado nuevo tampoco se puede elegir")
    void unRolPrivilegiadoNuevoTampoco() {
        // El caso que motiva la allowlist. Con el codigo anterior, que solo miraba
        // si el rol era ADMIN, este registro tendria exito y el usuario tendria un
        // rol privilegiado que el resto del sistema ni siquiera conoce.
        Long idRolNuevo = jdbc.queryForObject(
                "INSERT INTO usuarios.role(name, description) "
                        + "VALUES ('SUPERADMIN', 'Rol privilegiado inventado') RETURNING id",
                Long.class);

        assertThatThrownBy(() -> servicio.register(alta("SUPERADMIN")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM usuarios.app_user WHERE role_id = ?", Long.class, idRolNuevo))
                .isZero();
    }

    @Test
    @DisplayName("un rol que no existe se rechaza sin confirmar que nombres hay")
    void unRolInexistenteSeRechaza() {
        // Mismo mensaje que el de un rol real pero no registrable: si se distinguen,
        // el endpoint sirve para enumerar los nombres de la tabla role.
        Throwable real = catchThrowable(() -> servicio.register(alta("ADMIN")));
        Throwable inexistente = catchThrowable(() -> servicio.register(alta("NO_EXISTE")));

        assertThat(inexistente).isInstanceOf(IllegalArgumentException.class);
        assertThat(inexistente.getMessage()).isEqualTo(real.getMessage());
    }

    @Test
    @DisplayName("el rol se normaliza antes de decidir")
    void elRolSeNormaliza() {
        assertThat(servicio.register(alta("  professional  ")).getRoleName())
                .isEqualTo("PROFESSIONAL");
        // Y no al reves: minusculas con espacios de admin tampoco cuelan.
        assertThatThrownBy(() -> servicio.register(alta(" admin ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private AppUserRegisterRequestDto alta(String roleName) {
        AppUserRegisterRequestDto dto = new AppUserRegisterRequestDto();
        dto.setEmail("alta-" + roleName.toLowerCase().trim() + "@test.local");
        dto.setName("Alta " + roleName);
        dto.setPassword("ClaveValida1");
        dto.setRoleName(roleName);
        return dto;
    }

    private Throwable catchThrowable(Runnable r) {
        try {
            r.run();
            return new IllegalStateException("se esperaba una excepcion");
        } catch (RuntimeException e) {
            return e;
        }
    }
}
