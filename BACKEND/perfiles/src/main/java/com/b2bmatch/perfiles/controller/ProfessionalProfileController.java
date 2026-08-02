package com.b2bmatch.perfiles.controller;

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

import com.b2bmatch.perfiles.dto.ProfessionalProfileRequest;
import com.b2bmatch.perfiles.dto.ProfessionalProfileResponse;
import com.b2bmatch.perfiles.service.ProfessionalProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/professional-profiles")
@RequiredArgsConstructor
public class ProfessionalProfileController {

    private final ProfessionalProfileService service;

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private void requireOwner(Authentication authentication, Long profileUserId) {
        if (!isAdmin(authentication) && !profileUserId.equals(currentUserId(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño del perfil puede modificarlo");
        }
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalProfileResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalProfileResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfessionalProfileResponse> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ProfessionalProfileResponse> create(@Valid @RequestBody ProfessionalProfileRequest request, Authentication authentication) {
        if (!isAdmin(authentication)) {
            request.setUserId(currentUserId(authentication));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalProfileResponse> update(@PathVariable Long id, @Valid @RequestBody ProfessionalProfileRequest request, Authentication authentication) {
        requireOwner(authentication, service.findById(id).getUserId());
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        requireOwner(authentication, service.findById(id).getUserId());
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
