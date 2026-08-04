package com.b2bmatch.perfiles.dto;

import com.b2bmatch.perfiles.model.CompanyProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileResponse {

    private Long id;
    private Long userId;
    private String companyName;
    private String taxId;
    private String industry;
    private String website;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String companyDescription;
    private String logoUrl;
    private String status;

    public static CompanyProfileResponse fromEntity(CompanyProfile entity) {
        CompanyProfileResponse dto = new CompanyProfileResponse();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setTaxId(entity.getTaxId());
        dto.setIndustry(entity.getIndustry());
        dto.setWebsite(entity.getWebsite());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setCompanyDescription(entity.getCompanyDescription());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setStatus(entity.getStatus());
        return dto;
    }

}
