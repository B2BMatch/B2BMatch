package com.b2bmatch.perfiles.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalProfileRequest {

    @NotBlank(message = "userId is required")
    private Long userId;

    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    private String phone;
    private String biography;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private String portfolioUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String city;
    private String country;

}
