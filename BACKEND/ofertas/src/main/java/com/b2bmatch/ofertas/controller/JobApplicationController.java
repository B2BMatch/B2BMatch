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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<List<JobApplicationResponse>> findByJobOfferId(@PathVariable Long jobOfferId) {
        return ResponseEntity.ok(service.findByJobOfferId(jobOfferId));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<JobApplicationResponse>> findByProfessionalId(@PathVariable Long professionalId) {
        return ResponseEntity.ok(service.findByProfessionalId(professionalId));
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(@Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(@PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<JobApplicationResponse> accept(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.accept(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<JobApplicationResponse> reject(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.reject(id));
    }
}
