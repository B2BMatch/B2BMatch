package com.b2bmatch.perfiles.dto;

import java.math.BigDecimal;

import com.b2bmatch.perfiles.model.ProfessionalProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalProfileResponse {

    private Long id;
    private Long userId;
    private String firstName;
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
    private String status;

    public static ProfessionalProfileResponse fromEntity(ProfessionalProfile entity) {
        ProfessionalProfileResponse dto = new ProfessionalProfileResponse();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setPhone(entity.getPhone());
        dto.setBiography(entity.getBiography());
        dto.setExperienceYears(entity.getExperienceYears());
        dto.setHourlyRate(entity.getHourlyRate());
        dto.setPortfolioUrl(entity.getPortfolioUrl());
        dto.setLinkedinUrl(entity.getLinkedinUrl());
        dto.setGithubUrl(entity.getGithubUrl());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setStatus(entity.getStatus());
        return dto;
    }

}
