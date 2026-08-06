package com.b2bmatch.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppUserRegisterRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank
    @Pattern(regexp = "CUSTOMER|PROFESSIONAL|COMPANY", message = "Rol inválido")
    private String role;

    private String firstName;
    private String lastName;
    private String phone;

    private String companyName;
    private String taxId;
    private String industry;
}
