package com.b2bmatch.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppUserUpdateRequestDto {

    @Email
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private Long roleId;
}