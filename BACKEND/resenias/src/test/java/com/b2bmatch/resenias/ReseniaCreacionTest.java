package com.b2bmatch.resenias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.b2bmatch.resenias.exception.ForbiddenException;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.service.ReviewService;
import com.b2bmatch.resenias.support.AbstractIntegrationTest;

/**
 * Reglas de `createReview`: quien puede, sobre quien y con que paso previo.
 *
 * Eran las que el modulo dejaba sin ninguna verificacion, y todas se apoyan en
 * SQL contra `perfiles`, `ofertas` y `catalogo`: por eso las fixtures declaran
 * esas cinco tablas. El orden de las guardas tambien es contrato, porque cada
 * una cambia el mensaje que ve quien llama.
 */
@DisplayName("Creacion de resenias")
class ReseniaCreacionTest extends AbstractIntegrationTest {

    @Autowired
    private ReviewService resenas;

    @Test
    @DisplayName("un profesional no puede resenar")
    void unProfesionalNoPuedeResenar() {
        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 5, "Buen trabajo"), profesional, "PROFESSIONAL"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("con transaccion completada, el cliente puede resenar")
    void conTransaccionCompletadaPuedeResenar() {
        crearCotizacionAceptada(cliente, perfilProfesional);

        Review creada = resenas.createReview(resena(perfilProfesional, 5, "Excelente"), cliente, "CUSTOMER");

        assertThat(creada.getId()).isNotNull();
        // El autor sale del token, nunca del cuerpo de la peticion.
        assertThat(creada.getUserId()).isEqualTo(cliente);
        assertThat(creada.getProfessionalId()).isEqualTo(perfilProfesional);
        assertThat(creada.getCreatedAt()).isNotNull();
        assertThat(estaBorrada(creada.getId())).isFalse();
    }

    @Test
    @DisplayName("sin transaccion completada, no puede resenar")
    void sinTransaccionNoPuedeResenar() {
        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 5, "Excelente"), cliente, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transacción previa");
    }

    @Test
    @DisplayName("la postulacion ACCEPTED vale como transaccion previa de una empresa")
    void laPostulacionAceptadaValeParaLaEmpresa() {
        crearPostulacionAceptada(empresa, profesional);

        Review creada = resenas.createReview(resena(perfilProfesional, 4, "Buen equipo"), empresa, "COMPANY");

        assertThat(creada.getUserId()).isEqualTo(empresa);
    }

    @Test
    @DisplayName("una postulacion no aceptada no cuenta como transaccion previa")
    void unaPostulacionPendienteNoCuenta() {
        Long ofertaId = jdbc.queryForObject(
                "INSERT INTO ofertas.job_offer(user_id) VALUES (?) RETURNING id", Long.class, empresa);
        jdbc.update("INSERT INTO ofertas.application_table(job_offer_id, user_id, status) VALUES (?, ?, 'PENDING')",
                ofertaId, profesional);

        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 4, "Buen equipo"), empresa, "COMPANY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transacción previa");
    }

    @Test
    @DisplayName("una cotizacion de otro servicio del mismo profesional si cuenta")
    void unaCotizacionDeOtroServicioCuenta() {
        // El cruce es quotation -> professional_service -> professional_profile:
        // comprueba que la transaccion sea con ESE profesional, no con cualquiera
        // que tenga alguna cotizacion aceptada.
        crearCotizacionAceptada(cliente, perfilOtroProfesional);

        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 5, "Excelente"), cliente, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transacción previa");

        crearCotizacionAceptada(cliente, perfilProfesional);
        assertThat(resenas.createReview(resena(perfilProfesional, 5, "Excelente"), cliente, "CUSTOMER").getId()).isNotNull();
    }

    @Test
    @DisplayName("no se puede resenar sobre el propio perfil")
    void noSePuedeResenarSobreSiMismo() {
        // El dueno del perfil profesional intenta resenarse a si mismo. El chequeo
        // va antes del de transaccion previa, asi que no hace falta ninguna.
        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 5, "Yo"), profesional, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tu propio perfil");
    }

    @Test
    @DisplayName("no se puede resenar a un profesional dado de baja")
    void noSePuedeResenarAUnProfesionalDadoDeBaja() {
        Long retirado = crearUsuario("retirado@test.local");
        Long perfilRetirado = crearPerfilProfesionalDadoDeBaja(retirado);

        assertThatThrownBy(() -> resenas.createReview(resena(perfilRetirado, 5, "Buen trabajo"), cliente, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe o no está activo");
        assertThat(resenas.getReviewsByProfessional(perfilRetirado)).isEmpty();
    }

    @Test
    @DisplayName("el duplicado se rechaza mientras la reseña este viva")
    void elDuplicadoSeRechazaConResenasVivas() {
        crearCotizacionAceptada(cliente, perfilProfesional);
        resenas.createReview(resena(perfilProfesional, 5, "Primera"), cliente, "CUSTOMER");

        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 4, "Segunda"), cliente, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya dejaste una reseña");
    }

    @Test
    @DisplayName("el rating se valida en el servicio y el id no se acepta al crear")
    void elRatingSeValidaEnElServicio() {
        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 0, "Cero"), admin, "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating");
        assertThatThrownBy(() -> resenas.createReview(resena(perfilProfesional, 6, "Seis"), admin, "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating");

        Review conId = resena(perfilProfesional, 5, "Con id");
        conId.setId(42L);
        assertThatThrownBy(() -> resenas.createReview(conId, admin, "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No puedes indicar un id");
    }
}
