package com.b2bmatch.perfiles.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalProfileRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    private String phone;
    private String biography;

    @PositiveOrZero(message = "experienceYears no puede ser negativo")
    private Integer experienceYears;

    @PositiveOrZero(message = "hourlyRate no puede ser negativo")
    private BigDecimal hourlyRate;

    private String portfolioUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String city;
    private String country;

}
