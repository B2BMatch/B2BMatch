package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.ofertas.dto.JobOfferRequest;
import com.b2bmatch.ofertas.dto.JobOfferResponse;
import com.b2bmatch.ofertas.model.JobOffer;
import com.b2bmatch.ofertas.repository.JobOfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferRepository repository;

    public List<JobOfferResponse> findAll() {
        return repository.findByStatusNot("DELETED").stream()
                .map(JobOfferResponse::fromEntity)
                .toList();
    }

    public JobOfferResponse findById(Long id) {
        JobOffer entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + id));
        return JobOfferResponse.fromEntity(entity);
    }

    public List<JobOfferResponse> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId).stream()
                .filter(offer -> !"DELETED".equals(offer.getStatus()))
                .map(JobOfferResponse::fromEntity)
                .toList();
    }

    public JobOfferResponse create(JobOfferRequest request, Long currentUserId, boolean isAdmin) {
        JobOffer entity = new JobOffer();
        entity.setCompanyId(request.getCompanyId());
        entity.setUserId(isAdmin && request.getUserId() != null ? request.getUserId() : currentUserId);
        entity.setCategoryId(request.getCategoryId());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setBudget(request.getBudget());
        entity.setDeadline(request.getDeadline());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        } else {
            entity.setStatus("ACTIVE");
        }
        entity.setCreatedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(repository.save(entity));
    }

    public JobOfferResponse update(Long id, JobOfferRequest request, Long currentUserId, boolean isAdmin) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + id));
        requireOwner(existing.getUserId(), currentUserId, isAdmin);
        existing.setCategoryId(request.getCategoryId());
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setBudget(request.getBudget());
        existing.setDeadline(request.getDeadline());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + id));
        requireOwner(existing.getUserId(), currentUserId, isAdmin);
        existing.setStatus("DELETED");
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }

    public JobOfferResponse updateStatus(Long id, String status, Long currentUserId, boolean isAdmin) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + id));
        requireOwner(existing.getUserId(), currentUserId, isAdmin);
        existing.setStatus(status.toUpperCase());
        existing.setUpdatedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(repository.save(existing));
    }

    private void requireOwner(Long ownerUserId, Long currentUserId, boolean isAdmin) {
        if (!isAdmin && !ownerUserId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño de la oferta puede modificarla");
        }
    }
}
