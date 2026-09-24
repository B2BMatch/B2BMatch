package com.b2bmatch.resenias.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequestDto {

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    @NotNull(message = "rating es obligatorio")
    @Min(value = 1, message = "rating debe ser al menos 1")
    @Max(value = 5, message = "rating no puede ser mayor a 5")
    private Integer rating;

    private String comment;
}