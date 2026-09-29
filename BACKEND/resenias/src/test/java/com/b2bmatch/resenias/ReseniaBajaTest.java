package com.b2bmatch.resenias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.b2bmatch.resenias.exception.ForbiddenException;
import com.b2bmatch.resenias.exception.ResourceNotFoundException;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.repository.ReviewRepository;
import com.b2bmatch.resenias.service.ReviewService;
import com.b2bmatch.resenias.support.AbstractIntegrationTest;

/**
 * Baja de una reseña.
 *
 * Fija el contrato de la fase 2 en resenias: el borrado vive en `deleted_at` y
 * la fila no desaparece. Antes se hacia `reviewRepository.delete`, y una reseña
 * era contenido de usuario que no se podia recuperar; el `DELETE FROM
 * resenias.review` que hace la baja de usuario en usuarios/V901 lo confirma.
 *
 * `review` no tiene columna `status`, asi que no hay estado que preservar ni
 * `previous_status` que restaurar: a diferencia de usuarios, perfiles, catalogo
 * y ofertas, la baja no reescribe nada mas de la fila.
 */
@DisplayName("Baja de resenias")
class ReseniaBajaTest extends AbstractIntegrationTest {

    @Autowired
    private ReviewService resenas;

    @Autowired
    private ReviewRepository repositorio;

    @Test
    @DisplayName("borrar una reseña la da de baja sin borrar la fila")
    void borrarNoBorraLaFila() {
        Long id = crearResenia(cliente, perfilProfesional, 5, "Excelente trabajo");

        resenas.deleteReview(id, cliente, "CUSTOMER");

        assertThat(estaBorrada(id)).isTrue();
        assertThat(existeFila(id)).isTrue();
    }

    @Test
    @DisplayName("una reseña dada de baja desaparece de los cuatro lectores")
    void laResenaBorradaDesaparece() {
        Long id = crearResenia(cliente, perfilProfesional, 5, "Excelente trabajo");
        Long otra = crearResenia(otraClienta, perfilProfesional, 3, "Correcto");

        resenas.deleteReview(id, cliente, "CUSTOMER");

        assertThat(resenas.getAllReviews()).extracting("id").containsExactly(otra);
        assertThat(resenas.getReviewsByProfessional(perfilProfesional)).extracting("id").containsExactly(otra);
        assertThat(resenas.getReviewsByUser(cliente)).isEmpty();
        assertThatThrownBy(() -> resenas.getReview(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("no se borra dos veces")
    void noSeBorraDosVeces() {
        Long id = crearResenia(cliente, perfilProfesional, 4, "Bien");
        resenas.deleteReview(id, cliente, "CUSTOMER");

        assertThatThrownBy(() -> resenas.deleteReview(id, cliente, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está dada de baja");
    }

    @Test
    @DisplayName("la segunda baja no toca la fila, y no es solo por el if de Java")
    void laBajaEsCondicionalEnLaSentencia() {
        Long id = crearResenia(cliente, perfilProfesional, 4, "Bien");
        LocalDateTime primera = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime segunda = LocalDateTime.of(2026, 2, 2, 10, 0);

        // Directo al repositorio, que es donde vive la garantia: la segunda
        // llamada tiene que quedarse sin filas que tocar aunque la primera ya
        // hubiere dado de baja. Un `if` en el servicio no lo impediria, porque
        // dos peticiones simultaneas pasan las dos por ahi.
        assertThat(repositorio.marcarBaja(id, primera)).isEqualTo(1);
        assertThat(repositorio.marcarBaja(id, segunda)).isZero();

        assertThat(jdbc.queryForObject("SELECT deleted_at FROM resenias.review WHERE id = ?",
                LocalDateTime.class, id)).isEqualTo(primera);
    }

    @Test
    @DisplayName("el borrado queda marcado para no serializarse")
    void elBorradoNoSeExpone() throws Exception {
        // resenias devuelve la entidad y no un DTO, asi que sin @JsonIgnore cada
        // respuesta de la API empieza a llevar `deletedAt` hacia fuera, aunque sea
        // null. Es un cambio de contrato que nadie pidió y que ningún lector
        // necesita.
        //
        // Se comprueba la anotación y no un payload serializado porque este
        // módulo no expone ningun bean `ObjectMapper`: el mapper del convertidor
        // de MVC se construye internamente, asi que un `new ObjectMapper()` de
        // prueba no seria el de la API. Es el test más débil de los tres nuevos:
        // fija la decision, no la salida. Si algún dia se quiere el payload, la
        // via es un test de slice sobre el controlador.
        Field deletedAt = Review.class.getDeclaredField("deletedAt");

        assertThat(deletedAt.getAnnotation(JsonIgnore.class))
                .as("deleted_at no debe salir en el JSON de la API")
                .isNotNull();
    }

    @Test
    @DisplayName("una reseña dada de baja no se puede editar")
    void unaResenaDadaDeBajaNoSeEdita() {
        Long id = crearResenia(cliente, perfilProfesional, 4, "Comentario original");
        resenas.deleteReview(id, cliente, "CUSTOMER");

        assertThatThrownBy(() -> resenas.updateReview(id, resena(perfilProfesional, 1, "Comentario nuevo"), cliente, "CUSTOMER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no se puede modificar");
        // Editar no la resucita, ni por la via feliz ni con ADMIN.
        assertThat(estaBorrada(id)).isTrue();
        assertThatThrownBy(() -> resenas.updateReview(id, resena(perfilProfesional, 1, "Comentario nuevo"), admin, "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(estaBorrada(id)).isTrue();
    }

    @Test
    @DisplayName("un tercero no puede borrar ni editar una reseña ajena")
    void unTerceroNoPuedeTocarLaResena() {
        Long id = crearResenia(cliente, perfilProfesional, 4, "Bien");

        assertThatThrownBy(() -> resenas.deleteReview(id, otraClienta, "CUSTOMER"))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> resenas.updateReview(id, resena(perfilProfesional, 2, "Cambiada"), otraClienta, "CUSTOMER"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(estaBorrada(id)).isFalse();
    }

    @Test
    @DisplayName("ADMIN puede dar de baja la reseña de otro")
    void adminPuedeBorrarResenaAjena() {
        Long id = crearResenia(cliente, perfilProfesional, 4, "Bien");

        resenas.deleteReview(id, admin, "ADMIN");

        assertThat(estaBorrada(id)).isTrue();
    }

    @Test
    @DisplayName("tras la baja, el autor puede volver a resenar al mismo profesional")
    void trasLaBajaSePuedeVolverAResenar() {
        Long id = crearResenia(cliente, perfilProfesional, 1, "Me equivoque");
        resenas.deleteReview(id, cliente, "CUSTOMER");
        crearCotizacionAceptada(cliente, perfilProfesional);

        Long nueva = resenas.createReview(resena(perfilProfesional, 5, "Corrijo: excelente"), cliente, "CUSTOMER").getId();

        assertThat(resenas.getAllReviews()).extracting("id").containsExactly(nueva);
        assertThat(existeFila(id)).isTrue();
        assertThat(estaBorrada(id)).isTrue();
    }

    @Test
    @DisplayName("guardar la copia cargada no resucita una reseña dada de baja por otra peticion")
    @Transactional
    void guardarLaCopiaGestionadaNoResucita() {
        Long id = crearResenia(cliente, perfilProfesional, 4, "Bien");
        Review gestionada = repositorio.findById(id).orElseThrow();

        // Otra peticion da de baja la fila por fuera de esta unidad de trabajo,
        // que es lo que hace el usuario desde otra pestana. Sin
        // @DynamicUpdate, el guardado de abajo escribiria tambien
        // `deleted_at` en null y la resucitaria.
        jdbc.update("UPDATE resenias.review SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?", id);
        gestionada.setComment("Editado");
        repositorio.saveAndFlush(gestionada);

        assertThat(estaBorrada(id)).isTrue();
        assertThat(jdbc.queryForObject(
                "SELECT comment FROM resenias.review WHERE id = ?", String.class, id))
                .isEqualTo("Editado");
    }
}
