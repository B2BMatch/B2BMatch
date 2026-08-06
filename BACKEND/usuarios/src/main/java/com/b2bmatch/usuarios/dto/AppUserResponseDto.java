package com.b2bmatch.usuarios.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppUserResponseDto {

    private Long id;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
