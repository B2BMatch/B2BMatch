package com.b2bmatch.ofertas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferRequest {

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotBlank(message = "title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "budget is required")
    @Positive(message = "budget debe ser mayor a 0")
    private BigDecimal budget;

    @NotNull(message = "deadline is required")
    @Future(message = "deadline debe ser una fecha futura")
    private LocalDate deadline;
}
