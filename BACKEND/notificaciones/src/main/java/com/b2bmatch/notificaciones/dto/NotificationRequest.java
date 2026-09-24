package com.b2bmatch.notificaciones.dto;

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
public class NotificationRequest {

    @NotNull(message = "userId es obligatorio")
    private Long userId;

    @NotBlank(message = "title es obligatorio")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "message es obligatorio")
    @Size(max = 500, message = "message no puede superar los 500 caracteres")
    private String message;
}