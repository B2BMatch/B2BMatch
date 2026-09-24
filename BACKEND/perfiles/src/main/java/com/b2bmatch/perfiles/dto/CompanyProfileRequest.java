package com.b2bmatch.perfiles.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "companyName is required")
    @Size(max = 150)
    private String companyName;

    @NotBlank(message = "taxId is required")
    private String taxId;

    private String industry;
    private String website;

    @Email(message = "email no es válido")
    private String email;

    private String phone;
    private String address;
    private String city;
    private String country;
    private String companyDescription;

    private String logoUrl;

}
