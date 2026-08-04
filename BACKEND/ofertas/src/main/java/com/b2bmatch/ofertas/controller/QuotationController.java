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
import com.b2bmatch.ofertas.dto.QuotationRequest;
import com.b2bmatch.ofertas.dto.QuotationResponse;
import com.b2bmatch.ofertas.service.QuotationService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService service;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<QuotationResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<QuotationResponse>> findByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(service.findByServiceId(serviceId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<QuotationResponse>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.findByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<QuotationResponse> create(@Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotationResponse> update(@PathVariable Long id,
            @Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
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
    public ResponseEntity<QuotationResponse> accept(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.accept(id, claims.get("role", String.class)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<QuotationResponse> reject(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return ResponseEntity.ok(service.reject(id, claims.get("role", String.class)));
    }
}
