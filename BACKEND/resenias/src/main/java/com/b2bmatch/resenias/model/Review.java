package com.b2bmatch.resenias.model;
import java.time.LocalDateTime;

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

@Entity
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

    @Column (columnDefinition = "TEXT")
    private String comment;

    @Column (nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

}
