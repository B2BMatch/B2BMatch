package com.b2bmatch.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppUserStatusRequestDto {

    @NotBlank(message = "El estado es obligatorio")
    private String status;
}