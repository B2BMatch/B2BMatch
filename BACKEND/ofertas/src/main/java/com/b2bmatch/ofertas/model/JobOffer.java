package com.b2bmatch.ofertas.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DynamicUpdate
@Table(name = "job_offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    // `deleted_at` es la unica fuente de verdad sobre el borrado; `status` es
    // solo estado de negocio. `previous_status` guarda el estado previo para
    // que el borrado sea reversible en lugar de perderlo.
    // @DynamicUpdate limita el UPDATE a las columnas realmente modificadas, lo
    // que protege a una entidad **gestionada** cargada antes de que el cascade
    // de `usuarios` borrara la fila. No protege a una copia desactualizada que
    // se vuelve a guardar: `save()` sobre una entidad desligada hace merge, y el
    // merge marca como sucias todas las columnas que difieren, `deleted_at`
    // incluida, escribiendo un null y resucitando la fila.
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobOffer that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

