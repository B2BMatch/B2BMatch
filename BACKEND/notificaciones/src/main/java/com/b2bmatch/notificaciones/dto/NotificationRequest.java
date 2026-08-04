package com.b2bmatch.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "title is required")
    private String title;

    private String message;
}