package com.b2bmatch.ofertas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.ofertas.exception.ForbiddenException;
import com.b2bmatch.ofertas.exception.OfferNotFoundException;
import com.b2bmatch.ofertas.service.JobOfferService;
import com.b2bmatch.ofertas.support.AbstractIntegrationTest;

/**
 * Ciclo de vida de la baja de una oferta: borrar, que desaparezca de la
 * lectura, y el unico camino para revertirla.
 *
 * Estos tests fijan el contrato de la fase 2, que es justo lo que antes no
 * estaba cubierto:
 * <ul>
 * <li>borrar marca `deleted_at` y NO toca `status` (estado de negocio);</li>
 * <li>una oferta dada de baja desaparece de los tres caminos de lectura;</li>
 * <li>reactivar solo puede hacerlo un ADMIN, y es el unico que puede:
 * antes habia un guard que lo hacia inalcanzable.</li>
 * </ul>
 */
@DisplayName("Baja y reactivacion de ofertas")
class OfertaBajaTest extends AbstractIntegrationTest {

    @Autowired
    private JobOfferService service;

    @Test
    @DisplayName("borrar marca la baja y conserva el estado de negocio")
    void borrarMarcaLaBajaYConservaElEstado() {
        Long oferta = crearOferta(empresa, "ACTIVE");

        service.delete(oferta, empresa, "COMPANY");

        assertThat(jdbc.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM ofertas.job_offer WHERE id = ?",
                Boolean.class, oferta)).isTrue();
        // Lo importante: el estado de negocio sobrevive a la baja.
        assertThat(estadoOferta(oferta)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("una oferta dada de baja desaparece de los tres caminos de lectura")
    void laOfertaBorradaDesapareceDeLaLectura() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        Long otra = crearOferta(empresa, "ACTIVE");

        service.delete(oferta, empresa, "COMPANY");

        assertThat(service.findAll()).extracting("id").containsExactly(otra);
        assertThat(service.findByUserId(empresa)).extracting("id").containsExactly(otra);
        assertThatThrownBy(() -> service.findById(oferta)).isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    @DisplayName("borrar una oferta ya borrada responde que no existe")
    void noSePuedeBorrarDosVeces() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        service.delete(oferta, empresa, "COMPANY");

        assertThatThrownBy(() -> service.delete(oferta, empresa, "COMPANY"))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    @DisplayName("el dueno NO puede reactivar su oferta eliminada: solo ADMIN")
    void elDuenoNoPuedeReactivar() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        service.delete(oferta, empresa, "COMPANY");

        // Sin esto, cualquiera podria "des-borrar" su propia oferta y saltarse
        // la moderacion. La reactivacion es un acto de administracion.
        assertThatThrownBy(() -> service.updateStatus(oferta, "ACTIVE", empresa, "COMPANY"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(jdbc.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM ofertas.job_offer WHERE id = ?",
                Boolean.class, oferta)).isTrue();
    }

    @Test
    @DisplayName("ADMIN reactiva una oferta borrada (regresion del guard inalcanzable)")
    void adminReactivaLaOfertaBorrada() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        service.delete(oferta, empresa, "COMPANY");

        // Este es el camino que antes no existia: el guard de "borrada" en
        // updateStatus lo rechazaba para todo el mundo, admin incluido.
        var reactivada = service.updateStatus(oferta, "ACTIVE", admin, "ADMIN");

        assertThat(reactivada.getStatus()).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject(
                "SELECT deleted_at IS NULL FROM ofertas.job_offer WHERE id = ?",
                Boolean.class, oferta)).isTrue();
    }

    @Test
    @DisplayName("reactivar aplica el estado solicitado, no un estado inventado")
    void reactivarAplicaElEstadoSolicitado() {
        Long oferta = crearOferta(empresa, "ACTIVE");
        service.delete(oferta, empresa, "COMPANY");

        // Reactivar no es "deshacer el borrado": es cambiar de estado. El
        // endpoint levanta la baja y aplica el estado pedido, sin adivinar
        // ninguno previo. Si el admin pide SUSPENDED, asi queda.
        service.updateStatus(oferta, "SUSPENDED", admin, "ADMIN");

        assertThat(estadoOferta(oferta)).isEqualTo("SUSPENDED");
        // La baja se levanto igual: es el unico endpoint que puede hacerlo, y
        // no depende del estado que se pida.
        assertThat(jdbc.queryForObject(
                "SELECT deleted_at IS NULL FROM ofertas.job_offer WHERE id = ?",
                Boolean.class, oferta)).isTrue();
    }

    @Test
    @DisplayName("el estado de negocio se mantiene durante toda la baja")
    void elEstadoSobreviveALaBaja() {
        Long oferta = crearOferta(empresa, "INACTIVE");

        service.delete(oferta, empresa, "COMPANY");

        // Refuerzo del punto central de la fase 2: `status` no es la bandera de
        // borrado, y por eso una baja no puede corromper el estado.
        assertThat(estadoOferta(oferta)).isEqualTo("INACTIVE");
    }
}
