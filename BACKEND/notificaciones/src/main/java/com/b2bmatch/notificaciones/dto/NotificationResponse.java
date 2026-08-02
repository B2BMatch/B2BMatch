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
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification entity) {
        if (entity == null) return null;
        return new NotificationResponse(
            entity.getId(),
            entity.getUserId(),
            entity.getTitle(),
            entity.getMessage(),
            entity.getRead(),
            entity.getCreatedAt()
        );
    }
}
