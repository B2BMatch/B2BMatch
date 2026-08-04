package com.b2bmatch.ofertas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.ofertas.config.JwtService;
import com.b2bmatch.ofertas.dto.JobOfferRequest;
import com.b2bmatch.ofertas.dto.JobOfferResponse;
import com.b2bmatch.ofertas.service.JobOfferService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService service;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<JobOfferResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOfferResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobOfferResponse>> findByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(service.findByCompanyId(companyId));
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> create(@Valid @RequestBody JobOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobOfferResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody JobOfferRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        return ResponseEntity.ok(service.update(id, request, requesterId, requesterRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        service.delete(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }
}
