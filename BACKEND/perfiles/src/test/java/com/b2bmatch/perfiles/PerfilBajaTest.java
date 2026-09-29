package com.b2bmatch.perfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.perfiles.exception.ForbiddenException;
import com.b2bmatch.perfiles.exception.ProfileNotFoundException;
import com.b2bmatch.perfiles.dto.ProfessionalProfileRequest;
import com.b2bmatch.perfiles.service.CompanyProfileService;
import com.b2bmatch.perfiles.service.CustomerProfileService;
import com.b2bmatch.perfiles.service.ProfessionalProfileService;
import com.b2bmatch.perfiles.support.AbstractIntegrationTest;

/**
 * Baja y reactivacion de los tres tipos de perfil.
 *
 * Fija el contrato de la fase 2 en perfiles: el borrado vive en `deleted_at` y
 * no reescribe `status`. En estas tres tablas el CHECK de V1 solo admite
 * 'ACTIVE' y 'DELETED', asi que `status` no tiene estado de negocio que
 * preservar y la unica senal de baja posible es `deleted_at`.
 *
 * El ultimo test cubre la razon de ser de `@DynamicUpdate` en la entidad: sin
 * el, guardar una entidad **gestionada** cargada antes de que otra peticion
 * diera de baja el perfil escribiria tambien `deleted_at` en null y lo
 * resucitaria. No cubre la copia desligada, que un `save()` si revive: ver el
 * comentario de la entidad y el ultimo test, que fija la garantia que de
 * verdad existe, la del guard explicito del `update`.
 */
@DisplayName("Baja y reactivacion de perfiles")
class PerfilBajaTest extends AbstractIntegrationTest {

    @Autowired
    private ProfessionalProfileService profesionales;

    @Autowired
    private CompanyProfileService empresas;

    @Autowired
    private CustomerProfileService clientes;

    @Test
    @DisplayName("borrar un perfil no toca su estado")
    void borrarNoTocaElEstado() {
        Long id = crearPerfilProfesional(usuario);

        profesionales.delete(id, usuario, "PROFESSIONAL");

        assertThat(estaBorrada("professional_profile", id)).isTrue();
        assertThat(estado("professional_profile", id)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("un perfil dado de baja desaparece de listado, detalle y lectura por usuario")
    void elPerfilBorradoDesaparece() {
        Long id = crearPerfilProfesional(usuario);
        Long otro = crearPerfilProfesional(otroUsuario);

        profesionales.delete(id, usuario, "PROFESSIONAL");

        assertThat(profesionales.findAll(admin, "ADMIN")).extracting("id").containsExactly(otro);
        assertThatThrownBy(() -> profesionales.findById(id, admin, "ADMIN"))
                .isInstanceOf(ProfileNotFoundException.class);
        assertThatThrownBy(() -> profesionales.findByUserId(usuario, admin, "ADMIN"))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("reactivar levanta la baja sin inventar ningun estado previo")
    void reactivarLevantaLaBaja() {
        Long id = crearPerfilProfesional(usuario);
        profesionales.delete(id, usuario, "PROFESSIONAL");

        profesionales.reactivate(id, usuario, "PROFESSIONAL");

        assertThat(estaBorrada("professional_profile", id)).isFalse();
        assertThat(estado("professional_profile", id)).isEqualTo("ACTIVE");
        assertThat(profesionales.findById(id, usuario, "PROFESSIONAL").getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("no se borra dos veces ni se reactiva lo que esta vivo")
    void operacionesInvalidas() {
        Long id = crearPerfilProfesional(usuario);
        profesionales.delete(id, usuario, "PROFESSIONAL");

        assertThatThrownBy(() -> profesionales.delete(id, usuario, "PROFESSIONAL"))
                .isInstanceOf(IllegalArgumentException.class);
        profesionales.reactivate(id, usuario, "PROFESSIONAL");
        assertThatThrownBy(() -> profesionales.reactivate(id, usuario, "PROFESSIONAL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("un tercero no puede borrar ni reactivar un perfil ajeno")
    void unTerceroNoPuedeTocarElPerfil() {
        Long id = crearPerfilProfesional(usuario);

        assertThatThrownBy(() -> profesionales.delete(id, otroUsuario, "PROFESSIONAL"))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> profesionales.reactivate(id, otroUsuario, "PROFESSIONAL"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("el mismo contrato se cumple en empresa y cliente")
    void empresaYClienteSiguenElMismoContrato() {
        Long empresaId = crearPerfilEmpresa(usuario);
        Long clienteId = crearPerfilCliente(usuario);

        empresas.delete(empresaId, usuario, "COMPANY");
        clientes.delete(clienteId, usuario, "CUSTOMER");

        assertThat(estaBorrada("company_profile", empresaId)).isTrue();
        assertThat(estado("company_profile", empresaId)).isEqualTo("ACTIVE");
        assertThat(estaBorrada("customer_profile", clienteId)).isTrue();
        assertThat(estado("customer_profile", clienteId)).isEqualTo("ACTIVE");

        empresas.reactivate(empresaId, usuario, "COMPANY");
        clientes.reactivate(clienteId, usuario, "CUSTOMER");

        assertThat(estaBorrada("company_profile", empresaId)).isFalse();
        assertThat(estaBorrada("customer_profile", clienteId)).isFalse();
    }

    @Test
    @DisplayName("un perfil dado de baja no se puede editar, y editar no lo resucita")
    void editarUnPerfilBorradoNoLoResucita() {
        Long id = crearPerfilProfesional(usuario);
        profesionales.delete(id, usuario, "PROFESSIONAL");

        ProfessionalProfileRequest edicion = new ProfessionalProfileRequest();
        edicion.setFirstName("Ada");
        edicion.setLastName("Lovelace-Editada");

        // El camino real de edicion carga el perfil fresco y rechaza tocar uno
        // dado de baja. Esta es la garantia que importa: no la de
        // `@DynamicUpdate`, que solo protege a entidades gestionadas y no a una
        // copia desactualizada que se vuelve a guardar (ver el comentario de la
        // entidad).
        assertThatThrownBy(() -> profesionales.update(id, edicion, usuario, "PROFESSIONAL"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(estaBorrada("professional_profile", id)).isTrue();
    }
}
