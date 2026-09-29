package com.b2bmatch.resenias.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.resenias.model.Review;

/**
 * Ningun metodo sin filtro devuelve reseñas dadas de baja. `deleted_at` es la
 * unica fuente de verdad del borrado, asi que el filtro va en la consulta y no
 * en un `if` del servicio: un lector nuevo que se olvide de comprobarlo
 * entrega contenido invisible, que es el fallo que esta fase evita.
 */
public interface ReviewRepository extends JpaRepository <Review, Long>{

    //en ingles para seguir con la logica anterior
    List<Review> findByProfessionalIdAndDeletedAtIsNull(Long professionalId);

    List<Review> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Review> findByDeletedAtIsNull();

    // El duplicado se busca solo entre reseñas vivas: la dada de baja libera el
    // par, como hace el indice unico parcial de V901.
    boolean existsByUserIdAndProfessionalIdAndDeletedAtIsNull(Long userId, Long professionalId);

    /**
     * Da de baja la fila, y solo si sigue viva.
     *
     * El filtro va en el propio UPDATE, no en un `if` previo del servicio: dos
     * peticiones simultaneas que pasan las dos la comprobacion en Java dejan de
     * ser un caso abierto, porque la segunda sentencia ya no toca ninguna fila.
     * Devuelve 0 cuando la reseña ya estaba dada de baja, que es como el servicio
     * distingue el "ya estaba dada de baja" del "no existe".
     *
     * `@Transactional` es obligatorio en un metodo @Modifying: no hereda la
     * transaccion por defecto de SimpleJpaRepository, que solo cubre los
     * metodos que hereda de CrudRepository.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Review r SET r.deletedAt = :baja, r.updatedAt = :baja "
            + "WHERE r.id = :id AND r.deletedAt IS NULL")
    int marcarBaja(@Param("id") Long id, @Param("baja") LocalDateTime baja);

}
