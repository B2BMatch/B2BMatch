package com.b2bmatch.ofertas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.ofertas.dto.QuotationRequest;
import com.b2bmatch.ofertas.dto.QuotationResponse;
import com.b2bmatch.ofertas.service.QuotationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService service;

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    @GetMapping
    public ResponseEntity<List<QuotationResponse>> findAll(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede ver todas las cotizaciones");
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationResponse> findById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.findById(id, currentUserId(authentication), isAdmin(authentication)));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<QuotationResponse>> findByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(service.findByServiceId(serviceId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<QuotationResponse>> findByCustomerId(@PathVariable Long customerId, Authentication authentication) {
        return ResponseEntity.ok(service.findByCustomerId(customerId, currentUserId(authentication), isAdmin(authentication)));
    }

    @PostMapping
    public ResponseEntity<QuotationResponse> create(@Valid @RequestBody QuotationRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUserId(authentication), isAdmin(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotationResponse> update(@PathVariable Long id, @Valid @RequestBody QuotationRequest request, Authentication authentication) {
        return ResponseEntity.ok(service.update(id, request, currentUserId(authentication), isAdmin(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, currentUserId(authentication), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }
}
