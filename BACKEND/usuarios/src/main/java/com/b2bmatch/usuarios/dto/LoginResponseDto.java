package com.b2bmatch.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String token;
    private String email;
    private String roleName;
}