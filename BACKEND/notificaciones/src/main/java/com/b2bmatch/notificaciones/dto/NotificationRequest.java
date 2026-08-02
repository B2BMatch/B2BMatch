package com.b2bmatch.notificaciones.dto;

import java.time.LocalDateTime;

import com.b2bmatch.notificaciones.model.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private Long userId;

    private String title;

    private String message;
}
