package com.b2bmatch.resenias.model;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column (nullable = false)
    private Long customer_id; //puede cambiarse a customerId para seguir convencion, se tendria que agregar name = "customer_id" a parametro de la columna.

    @Column (nullable = false)
    private Long professional_id;

    @Column
    private int rating;

    @Column
    private String comment;

    @Column
    private LocalDateTime created_at;

    @Column
    private LocalDateTime updated_at;

}
