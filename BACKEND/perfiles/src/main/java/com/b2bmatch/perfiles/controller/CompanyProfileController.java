package com.b2bmatch.perfiles.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.perfiles.config.JwtService;
import com.b2bmatch.perfiles.dto.CompanyProfileRequest;
import com.b2bmatch.perfiles.dto.CompanyProfileResponse;
import com.b2bmatch.perfiles.exception.ForbiddenException;
import com.b2bmatch.perfiles.service.CompanyProfileService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company-profiles")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService service;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<CompanyProfileResponse>> findAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Claims claims = optionalClaims(authHeader);
        Long requesterId = claims != null ? claims.get("userId", Long.class) : null;
        String requesterRole = claims != null ? claims.get("role", String.class) : null;
        return ResponseEntity.ok(service.findAll(requesterId, requesterRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyProfileResponse> findById(@PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Claims claims = optionalClaims(authHeader);
        Long requesterId = claims != null ? claims.get("userId", Long.class) : null;
        String requesterRole = claims != null ? claims.get("role", String.class) : null;
        return ResponseEntity.ok(service.findById(id, requesterId, requesterRole));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CompanyProfileResponse> findByUserId(@PathVariable("userId") Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Claims claims = optionalClaims(authHeader);
        Long requesterId = claims != null ? claims.get("userId", Long.class) : null;
        String requesterRole = claims != null ? claims.get("role", String.class) : null;
        return ResponseEntity.ok(service.findByUserId(userId, requesterId, requesterRole));
    }

    @PostMapping
    public ResponseEntity<CompanyProfileResponse> create(@Valid @RequestBody CompanyProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        if (!"ADMIN".equals(requesterRole)) {
            if (!"COMPANY".equals(requesterRole)) {
                throw new ForbiddenException("Solo las empresas pueden registrar un perfil de empresa");
            }
            request.setUserId(requesterId);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyProfileResponse> update(@PathVariable("id") Long id,
            @Valid @RequestBody CompanyProfileRequest request,
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
    public ResponseEntity<CompanyProfileResponse> reactivate(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        return ResponseEntity.ok(service.reactivate(id, requesterId, requesterRole));
    }
}
