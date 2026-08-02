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

import com.b2bmatch.ofertas.dto.JobApplicationRequest;
import com.b2bmatch.ofertas.dto.JobApplicationResponse;
import com.b2bmatch.ofertas.service.JobApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService service;

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> findAll(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede ver todas las postulaciones");
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.findById(id, currentUserId(authentication), isAdmin(authentication)));
    }

    @GetMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<List<JobApplicationResponse>> findByJobOfferId(@PathVariable Long jobOfferId, Authentication authentication) {
        return ResponseEntity.ok(service.findByJobOfferId(jobOfferId, currentUserId(authentication), isAdmin(authentication)));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<JobApplicationResponse>> findByProfessionalId(@PathVariable Long professionalId, Authentication authentication) {
        return ResponseEntity.ok(service.findByProfessionalId(professionalId, currentUserId(authentication), isAdmin(authentication)));
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(@Valid @RequestBody JobApplicationRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUserId(authentication), isAdmin(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequest request, Authentication authentication) {
        return ResponseEntity.ok(service.update(id, request, currentUserId(authentication), isAdmin(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, currentUserId(authentication), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }
}
