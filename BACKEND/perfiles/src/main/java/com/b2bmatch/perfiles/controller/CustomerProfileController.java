package com.b2bmatch.perfiles.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.b2bmatch.perfiles.config.JwtService;

import com.b2bmatch.perfiles.dto.CustomerProfileRequest;
import com.b2bmatch.perfiles.dto.CustomerProfileResponse;
import com.b2bmatch.perfiles.service.CustomerProfileService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer-profiles")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService service;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<CustomerProfileResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerProfileResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerProfileResponse> findByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CustomerProfileResponse> create(@Valid @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerProfileResponse> update(@PathVariable("id") Long id,
            @Valid @RequestBody CustomerProfileRequest request) {
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
public ResponseEntity<CustomerProfileResponse> reactivate(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authHeader) {

    String token = authHeader.substring(7);
    Claims claims = jwtService.parseToken(token);

    Long requesterId = claims.get("userId", Long.class);
    String requesterRole = claims.get("role", String.class);

    return ResponseEntity.ok(service.reactivate(id, requesterId, requesterRole));
}
}