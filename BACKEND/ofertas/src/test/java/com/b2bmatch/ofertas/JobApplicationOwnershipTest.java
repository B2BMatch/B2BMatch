package com.b2bmatch.ofertas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.b2bmatch.ofertas.exception.ForbiddenException;
import com.b2bmatch.ofertas.service.JobApplicationService;
import com.b2bmatch.ofertas.support.AbstractIntegrationTest;

@DisplayName("Reglas de propiedad sobre postulaciones")
class JobApplicationOwnershipTest extends AbstractIntegrationTest {

    @Autowired
    JobApplicationService service;

    @Test
    @DisplayName("solo la empresa dueña ve las postulaciones de su oferta")
    void soloDuenioVePostulacionesDeSuOferta() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        crearPostulacion(oferta, profesional, "PENDING");

        assertThatThrownBy(() -> service.findByJobOfferId(oferta, otraEmpresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);

        assertThat(service.findByJobOfferId(oferta, empresa, "COMPANY")).hasSize(1);
    }

    @Test
    @DisplayName("un profesional no puede ver las postulaciones de otro")
    void noPuedeVerPostulacionesAjenas() {
        assertThatThrownBy(() -> service.findByUserId(profesional, otroProfesional, "PROFESSIONAL"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("solo ADMIN puede listar todas las postulaciones")
    void listadoGlobalSoloAdmin() {
        assertThatThrownBy(() -> service.findAll("COMPANY"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(service.findAll("ADMIN")).isEmpty();
    }

    @Test
    @DisplayName("una empresa no puede postularse a una oferta")
    void empresaNoPuedePostularse() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        assertThatThrownBy(() -> service.create(solicitudPostulacion(oferta), otraEmpresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("nadie puede postularse a una oferta que no esta ACTIVE")
    void ofertaNoActivaRechazaPostulacion() {
        Long cerrada = crearOferta(empresa, "CLOSED");

        assertThatThrownBy(() -> service.create(solicitudPostulacion(cerrada), profesional, "PROFESSIONAL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("un profesional no puede postularse dos veces a la misma oferta")
    void noPuedePostularseDosVeces() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        service.create(solicitudPostulacion(oferta), profesional, "PROFESSIONAL");

        assertThatThrownBy(() -> service.create(solicitudPostulacion(oferta), profesional, "PROFESSIONAL"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("solo la empresa dueña puede aceptar una postulacion")
    void soloDuenioAcepta() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long postulacion = crearPostulacion(oferta, profesional, "PENDING");

        assertThatThrownBy(() -> service.accept(postulacion, otraEmpresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(estadoPostulacion(postulacion)).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("aceptar una postulacion cierra la oferta y rechaza las demas")
    void aceptarCierraLaOfertaYRechazaElResto() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long aceptada = crearPostulacion(oferta, profesional, "PENDING");
        Long otra = crearPostulacion(oferta, otroProfesional, "PENDING");

        service.accept(aceptada, empresa, "COMPANY");

        assertThat(estadoPostulacion(aceptada)).isEqualTo("ACCEPTED");
        assertThat(estadoPostulacion(otra)).isEqualTo("REJECTED");
        assertThat(estadoOferta(oferta)).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("con la oferta ya cerrada no se puede aceptar una segunda postulacion")
    void noSePuedeAceptarDosVeces() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long primera = crearPostulacion(oferta, profesional, "PENDING");
        Long segunda = crearPostulacion(oferta, otroProfesional, "PENDING");

        service.accept(primera, empresa, "COMPANY");

        assertThatThrownBy(() -> service.accept(segunda, empresa, "COMPANY"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(estadoOferta(oferta)).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("no se puede mover una postulacion a otra oferta")
    void noSePuedeMoverDeOferta() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long otraOferta = crearOferta(otraEmpresa, "ACTIVE");
        Long postulacion = crearPostulacion(oferta, profesional, "PENDING");

        assertThatThrownBy(
                () -> service.update(postulacion, solicitudPostulacion(otraOferta), profesional, "PROFESSIONAL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("una postulacion ya aceptada no se puede editar")
    void postulacionAceptadaNoSeEdita() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long postulacion = crearPostulacion(oferta, profesional, "ACCEPTED");

        assertThatThrownBy(
                () -> service.update(postulacion, solicitudPostulacion(oferta), profesional, "PROFESSIONAL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("solo quien postulo puede borrar su postulacion")
    void soloElPostulanteBorra() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long postulacion = crearPostulacion(oferta, profesional, "PENDING");

        assertThatThrownBy(() -> service.delete(postulacion, otroProfesional, "PROFESSIONAL"))
                .isInstanceOf(ForbiddenException.class);

        service.delete(postulacion, profesional, "PROFESSIONAL");
        assertThat(service.findAll("ADMIN")).isEmpty();
    }
}
