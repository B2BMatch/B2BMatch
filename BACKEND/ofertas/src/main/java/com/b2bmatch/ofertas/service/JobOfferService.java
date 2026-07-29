package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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

    public JobOfferResponse create(JobOfferRequest request) {
        JobOffer entity = new JobOffer();
        entity.setCompanyId(request.getCompanyId());
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

    public JobOfferResponse update(Long id, JobOfferRequest request) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + id));
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

    public void delete(Long id) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + id));
        existing.setStatus("DELETED");
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }
}

