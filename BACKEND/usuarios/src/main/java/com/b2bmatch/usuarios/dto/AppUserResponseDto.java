package com.b2bmatch.usuarios.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AppUserResponseDto {
    private Long id;
    private String email;
    private String status;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}