package com.b2bmatch.resenias.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "userId es obligatorio")
    private Long userId;

    @Column (nullable = false)
    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    @Column (nullable = false)
    @NotNull(message = "rating es obligatorio")
    @Min(value = 1, message = "rating debe ser al menos 1")
    @Max(value = 5, message = "rating no puede ser mayor a 5")
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // `deleted_at` es la unica fuente de verdad sobre el borrado: la fila sigue
    // existiendo y todos los lectores filtran por `deleted_at IS NULL`. No hay
    // `previous_status` porque `review` no tiene columna `status`.
    // @DynamicUpdate limita el UPDATE a las columnas realmente modificadas, lo
    // que protege a una entidad **gestionada** cargada antes de que otra
    // peticion hubiera dado de baja la misma fila: sin el, el guardado
    // escribiria tambien `deleted_at` en null y la resucitaria. No protege a una
    // copia desactualizada que se vuelve a guardar: `save()` sobre una entidad
    // desligada hace merge, y el merge marca como sucias todas las columnas
    // que difieren, `deleted_at` incluida.
    // @JsonIgnore porque resenias devuelve la entidad y no un DTO: sin esto, cada
    // respuesta de la API empieza a llevar `deletedAt` hacia fuera, y eso es un
    // cambio de contrato que nadie pidió. Los otros cuatro servicios no lo
    // exponen porque filtran en el DTO, no por esto. Ningún lector lo necesita:
    // una reseña dada de baja no sale de ninguna consulta.
    @JsonIgnore
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
