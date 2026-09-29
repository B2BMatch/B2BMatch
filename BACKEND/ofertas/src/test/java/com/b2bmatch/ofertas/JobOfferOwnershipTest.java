package com.b2bmatch.ofertas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.ofertas.exception.ForbiddenException;
import com.b2bmatch.ofertas.service.JobOfferService;
import com.b2bmatch.ofertas.support.AbstractIntegrationTest;

@DisplayName("Reglas de propiedad sobre ofertas de trabajo")
class JobOfferOwnershipTest extends AbstractIntegrationTest {

    @Autowired
    JobOfferService service;

    @Test
    @DisplayName("una empresa no puede editar la oferta de otra empresa")
    void noPuedeEditarOfertaAjena() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        assertThatThrownBy(() -> service.update(oferta, solicitudOferta(), otraEmpresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("una empresa no puede eliminar la oferta de otra empresa")
    void noPuedeEliminarOfertaAjena() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        assertThatThrownBy(() -> service.delete(oferta, otraEmpresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(estadoOferta(oferta)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("el dueño si puede editar su propia oferta")
    void duenioPuedeEditar() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        var actualizada = service.update(oferta, solicitudOferta(), empresa, "COMPANY");

        assertThat(actualizada.getTitle()).isEqualTo("Oferta nueva");
    }

    @Test
    @DisplayName("ADMIN puede editar una oferta ajena")
    void adminPuedeEditarOfertaAjena() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        var actualizada = service.update(oferta, solicitudOferta(), admin, "ADMIN");

        assertThat(actualizada.getTitle()).isEqualTo("Oferta nueva");
    }

    @Test
    @DisplayName("un profesional no puede publicar ofertas")
    void profesionalNoPuedePublicar() {
        assertThatThrownBy(() -> service.create(solicitudOferta(), profesional, "PROFESSIONAL"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("una empresa sin perfil activo no puede publicar")
    void empresaSinPerfilNoPuedePublicar() {
        Long sinPerfil = crearUsuario("sin-perfil@test.local");

        assertThatThrownBy(() -> service.create(solicitudOferta(), sinPerfil, "COMPANY"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("no se puede publicar contra una categoria inexistente")
    void categoriaInexistente() {
        var solicitud = solicitudOferta();
        solicitud.setCategoryId(999_999L);

        assertThatThrownBy(() -> service.create(solicitud, empresa, "COMPANY"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("el dueño NO puede suspender su propia oferta: es accion de moderacion")
    void duenioNoPuedeSuspender() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        assertThatThrownBy(() -> service.updateStatus(oferta, "SUSPENDED", empresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(estadoOferta(oferta)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("el dueño NO puede reactivar una oferta que ADMIN suspendio")
    void duenioNoPuedeDesSuspender() {
        Long oferta = crearOferta(empresa, "SUSPENDED");

        assertThatThrownBy(() -> service.updateStatus(oferta, "ACTIVE", empresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(estadoOferta(oferta)).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("ADMIN si puede suspender y volver a activar")
    void adminModera() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        service.updateStatus(oferta, "SUSPENDED", admin, "ADMIN");
        assertThat(estadoOferta(oferta)).isEqualTo("SUSPENDED");

        service.updateStatus(oferta, "ACTIVE", admin, "ADMIN");
        assertThat(estadoOferta(oferta)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("el listado publico oculta las ofertas dadas de baja, cerradas y vencidas")
    void listadoPublicoFiltraEstados() {
        crearOferta(empresa, "ACTIVE");
        // Dada de baja de verdad: `deleted_at` marcada y status intacto, que es
        // como la marca el sistema desde que el borrado dejo de pisar `status`.
        crearOfertaDadaDeBaja(empresa, "ACTIVE");
        crearOferta(empresa, "CLOSED");
        crearOferta(empresa, "EXPIRED");

        assertThat(service.findAll()).hasSize(1);
    }
}
