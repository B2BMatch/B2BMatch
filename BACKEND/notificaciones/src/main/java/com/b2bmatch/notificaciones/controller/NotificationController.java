package com.b2bmatch.notificaciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.notificaciones.config.JwtService;
import com.b2bmatch.notificaciones.dto.NotificationRequest;
import com.b2bmatch.notificaciones.dto.NotificationResponse;
import com.b2bmatch.notificaciones.service.NotificationService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = jwtService.parseToken(authHeader.substring(7));
        String requesterRole = claims.get("role", String.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, requesterRole));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> findByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findByUserId(userId,
                claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> findUnreadByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findUnreadByUserId(userId,
                claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> findById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findById(id,
                claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.markAsRead(id,
                claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        service.delete(id, claims.get("userId", Long.class), claims.get("role", String.class));
        return ResponseEntity.noContent().build();
    }
}