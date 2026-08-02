package com.b2bmatch.notificaciones.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.notificaciones.dto.NotificationRequest;
import com.b2bmatch.notificaciones.dto.NotificationResponse;
import com.b2bmatch.notificaciones.model.Notification;
import com.b2bmatch.notificaciones.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public List<NotificationResponse> findAll() {
        return repository.findAll().stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public List<NotificationResponse> findByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public List<NotificationResponse> findUnreadByUserId(Long userId) {
        return repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public long countUnreadByUserId(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    public NotificationResponse findById(Long id) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        return NotificationResponse.fromEntity(entity);
    }

    public NotificationResponse create(NotificationRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        Notification entity = new Notification();
        entity.setUserId(request.getUserId());
        entity.setTitle(request.getTitle());
        entity.setMessage(request.getMessage());
        entity.setRead(false);
        entity.setCreatedAt(LocalDateTime.now());
        return NotificationResponse.fromEntity(repository.save(entity));
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        entity.setRead(true);
        return NotificationResponse.fromEntity(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
