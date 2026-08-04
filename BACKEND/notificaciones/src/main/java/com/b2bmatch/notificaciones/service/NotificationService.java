package com.b2bmatch.notificaciones.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.notificaciones.dto.NotificationRequest;
import com.b2bmatch.notificaciones.dto.NotificationResponse;
import com.b2bmatch.notificaciones.exception.ForbiddenException;
import com.b2bmatch.notificaciones.exception.NotificationNotFoundException;
import com.b2bmatch.notificaciones.model.Notification;
import com.b2bmatch.notificaciones.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationResponse create(NotificationRequest request, String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            throw new ForbiddenException("Solo ADMIN puede crear notificaciones directamente");
        }

        Notification entity = new Notification();
        entity.setUserId(request.getUserId());
        entity.setTitle(request.getTitle());
        entity.setMessage(request.getMessage());
        entity.setIsRead(false);
        entity.setCreatedAt(LocalDateTime.now());
        return NotificationResponse.fromEntity(repository.save(entity));
    }

    public List<NotificationResponse> findByUserId(Long userId, Long requesterId, String requesterRole) {
        checkOwnership(userId, requesterId, requesterRole);
        return repository.findByUserId(userId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public List<NotificationResponse> findUnreadByUserId(Long userId, Long requesterId, String requesterRole) {
        checkOwnership(userId, requesterId, requesterRole);
        return repository.findByUserIdAndIsReadFalse(userId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public NotificationResponse findById(Long id, Long requesterId, String requesterRole) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        checkOwnership(entity.getUserId(), requesterId, requesterRole);
        return NotificationResponse.fromEntity(entity);
    }

    public NotificationResponse markAsRead(Long id, Long requesterId, String requesterRole) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        checkOwnership(entity.getUserId(), requesterId, requesterRole);

        entity.setIsRead(true);
        return NotificationResponse.fromEntity(repository.save(entity));
    }

    public void delete(Long id, Long requesterId, String requesterRole) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        checkOwnership(entity.getUserId(), requesterId, requesterRole);
        repository.deleteById(id);
    }

    private void checkOwnership(Long ownerId, Long requesterId, String requesterRole) {
        boolean isOwner = ownerId.equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes ver/modificar tus propias notificaciones, o ser ADMIN");
        }
    }
}