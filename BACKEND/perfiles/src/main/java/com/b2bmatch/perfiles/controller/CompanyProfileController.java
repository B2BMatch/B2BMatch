package com.b2bmatch.perfiles.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.perfiles.config.JwtService;
import com.b2bmatch.perfiles.dto.CompanyProfileRequest;
import com.b2bmatch.perfiles.dto.CompanyProfileResponse;
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
    public ResponseEntity<List<CompanyProfileResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyProfileResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CompanyProfileResponse> findByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CompanyProfileResponse> create(@Valid @RequestBody CompanyProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyProfileResponse> update(@PathVariable("id") Long id,
            @Valid @RequestBody CompanyProfileRequest request) {
        return ResponseEntity.ok(service.update(id, request));
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