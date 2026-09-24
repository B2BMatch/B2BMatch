package com.b2bmatch.perfiles.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.perfiles.config.JwtService;
import com.b2bmatch.perfiles.dto.ProfessionalProfileRequest;
import com.b2bmatch.perfiles.dto.ProfessionalProfileResponse;
import com.b2bmatch.perfiles.exception.ForbiddenException;
import com.b2bmatch.perfiles.service.ProfessionalProfileService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/professional-profiles")
@RequiredArgsConstructor
public class ProfessionalProfileController {

    private final ProfessionalProfileService service;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<ProfessionalProfileResponse>> findAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Claims claims = optionalClaims(authHeader);
        Long requesterId = claims != null ? claims.get("userId", Long.class) : null;
        String requesterRole = claims != null ? claims.get("role", String.class) : null;
        return ResponseEntity.ok(service.findAll(requesterId, requesterRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalProfileResponse> findById(@PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Claims claims = optionalClaims(authHeader);
        Long requesterId = claims != null ? claims.get("userId", Long.class) : null;
        String requesterRole = claims != null ? claims.get("role", String.class) : null;
        return ResponseEntity.ok(service.findById(id, requesterId, requesterRole));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfessionalProfileResponse> findByUserId(@PathVariable("userId") Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Claims claims = optionalClaims(authHeader);
        Long requesterId = claims != null ? claims.get("userId", Long.class) : null;
        String requesterRole = claims != null ? claims.get("role", String.class) : null;
        return ResponseEntity.ok(service.findByUserId(userId, requesterId, requesterRole));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfessionalProfileResponse> me(
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        return ResponseEntity.ok(service.findByUserId(userId, userId, role));
    }

    @PostMapping
    public ResponseEntity<ProfessionalProfileResponse> create(@Valid @RequestBody ProfessionalProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        if (!"ADMIN".equals(requesterRole)) {
            if (!"PROFESSIONAL".equals(requesterRole)) {
                throw new ForbiddenException("Solo los profesionales pueden registrar un perfil profesional");
            }
            request.setUserId(requesterId);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalProfileResponse> update(@PathVariable("id") Long id,
            @Valid @RequestBody ProfessionalProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        return ResponseEntity.ok(service.update(id, request, requesterId, requesterRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        service.delete(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    private Claims optionalClaims(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            return jwtService.parseToken(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ProfessionalProfileResponse> reactivate(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        return ResponseEntity.ok(service.reactivate(id, requesterId, requesterRole));
    }
}
