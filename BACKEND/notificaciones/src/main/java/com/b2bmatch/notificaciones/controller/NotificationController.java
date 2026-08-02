package com.b2bmatch.notificaciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.notificaciones.dto.NotificationRequest;
import com.b2bmatch.notificaciones.dto.NotificationResponse;
import com.b2bmatch.notificaciones.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private void requireOwner(Authentication authentication, Long resourceUserId) {
        if (!isAdmin(authentication) && !resourceUserId.equals(currentUserId(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso sobre esta notificación");
        }
    }

    private void requireSelf(Authentication authentication, Long requestedUserId) {
        if (!isAdmin(authentication) && !requestedUserId.equals(currentUserId(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes acceder a tus propias notificaciones");
        }
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> findAll(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede ver todas las notificaciones");
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> findById(@PathVariable Long id, Authentication authentication) {
        NotificationResponse notification = service.findById(id);
        requireOwner(authentication, notification.getUserId());
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> findByUserId(@PathVariable Long userId, Authentication authentication) {
        requireSelf(authentication, userId);
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> findUnreadByUserId(@PathVariable Long userId, Authentication authentication) {
        requireSelf(authentication, userId);
        return ResponseEntity.ok(service.findUnreadByUserId(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> countUnreadByUserId(@PathVariable Long userId, Authentication authentication) {
        requireSelf(authentication, userId);
        return ResponseEntity.ok(service.countUnreadByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id, Authentication authentication) {
        NotificationResponse notification = service.findById(id);
        requireOwner(authentication, notification.getUserId());
        return ResponseEntity.ok(service.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        NotificationResponse notification = service.findById(id);
        requireOwner(authentication, notification.getUserId());
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
