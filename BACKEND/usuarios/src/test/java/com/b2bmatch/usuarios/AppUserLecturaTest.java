package com.b2bmatch.usuarios;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.usuarios.service.AppUserService;
import com.b2bmatch.usuarios.support.AbstractIntegrationTest;

/**
 * Lecturas de usuario y el rol que viajan en la respuesta.
 *
 * Todos los DTO de respuesta llevan el nombre del rol, y el rol es una asociacion
 * LAZY. Estos tests existen porque los servicios que hacen estas lecturas no son
 * transaccionales, asi que el unico motivo de que funcionen es que Spring Boot
 * tenga `open-in-view` en true, que es un default y no una decision. Estos tests
 * corren sin ese filtro, con lo que son el aviso previo al dia que alguien lo
 * desactive y el login, los listados y el cambio de estado empiecen a dar 500.
 */
@DisplayName("Lectura de usuarios y su rol")
class AppUserLecturaTest extends AbstractIntegrationTest {

    @Autowired
    private AppUserService servicio;

    @Test
    @DisplayName("findById devuelve el rol del usuario")
    void findByIdDevuelveElRol() {
        assertThat(servicio.findById(usuario).getRoleName()).isEqualTo("PROFESSIONAL");
    }

    @Test
    @DisplayName("findAll devuelve el rol de cada usuario, no solo el primero")
    void findAllDevuelveTodosLosRoles() {
        // El fixture deja tres usuarios con el mismo rol, asi que "devuelve bien" y
        // "devuelve bien el primero" se confundirian: se mira la lista entera.
        assertThat(servicio.findAll(false))
                .hasSize(3)
                .allSatisfy(dto -> assertThat(dto.getRoleName()).isEqualTo("PROFESSIONAL"));
    }

    @Test
    @DisplayName("findAll con las dadas de baja tambien devuelve el rol")
    void findAllConBajasDevuelveElRol() {
        servicio.delete(usuario);

        assertThat(servicio.findAll(true))
                .hasSize(3)
                .allSatisfy(dto -> assertThat(dto.getRoleName()).isEqualTo("PROFESSIONAL"));
    }

    @Test
    @DisplayName("findByRole devuelve el rol de cada usuario")
    void findByRoleDevuelveTodosLosRoles() {
        assertThat(servicio.findByRole("PROFESSIONAL"))
                .hasSize(3)
                .allSatisfy(dto -> assertThat(dto.getRoleName()).isEqualTo("PROFESSIONAL"));
    }

    @Test
    @DisplayName("cambiar el estado devuelve el usuario con su rol")
    void cambiarElEstadoDevuelveElRol() {
        // El metodo carga, muta y devuelve el DTO, y el save devuelve una entidad
        // que sale desligada: el rol tiene que venir ya resuelto de la carga.
        assertThat(servicio.updateStatus(usuario, "SUSPENDED").getRoleName())
                .isEqualTo("PROFESSIONAL");
    }

    @Test
    @DisplayName("las lecturas filtran las dadas de baja salvo que se pidan")
    void lasLecturasFiltranLasBajas() {
        servicio.delete(usuario);

        assertThat(servicio.findAll(false)).hasSize(2);
        assertThat(servicio.findByRole("PROFESSIONAL")).hasSize(2);
        // El detalle no filtra: GET /users/{id} tiene que poder devolver tambien una
        // cuenta dada de baja, que es como el admin la ve antes de reactivarla.
        assertThat(servicio.findById(usuario).getDeletedAt()).isNotNull();
    }
}
