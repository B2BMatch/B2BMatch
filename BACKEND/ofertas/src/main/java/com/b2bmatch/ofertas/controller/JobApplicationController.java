package com.b2bmatch.ofertas.controller;

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

import com.b2bmatch.ofertas.config.JwtService;
import com.b2bmatch.ofertas.dto.JobApplicationRequest;
import com.b2bmatch.ofertas.dto.JobApplicationResponse;
import com.b2bmatch.ofertas.service.JobApplicationService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService service;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> findAll(
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findAll(claims.get("role", String.class)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findById(id, claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @GetMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<List<JobApplicationResponse>> findByJobOfferId(
            @PathVariable Long jobOfferId,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findByJobOfferId(jobOfferId, claims.get("userId", Long.class),
                claims.get("role", String.class)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JobApplicationResponse>> findByUserId(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.findByUserId(userId, claims.get("userId", Long.class),
                claims.get("role", String.class)));
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @Valid @RequestBody JobApplicationRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request, claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(@PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.update(id, request, claims.get("userId", Long.class),
                claims.get("role", String.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        service.delete(id, claims.get("userId", Long.class), claims.get("role", String.class));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<JobApplicationResponse> accept(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.accept(id, claims.get("userId", Long.class), claims.get("role", String.class)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<JobApplicationResponse> reject(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.reject(id, claims.get("userId", Long.class), claims.get("role", String.class)));
    }
}
