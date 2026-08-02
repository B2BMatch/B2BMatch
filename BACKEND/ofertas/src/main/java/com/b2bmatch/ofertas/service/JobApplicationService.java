package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.ofertas.dto.JobApplicationRequest;
import com.b2bmatch.ofertas.dto.JobApplicationResponse;
import com.b2bmatch.ofertas.model.JobApplication;
import com.b2bmatch.ofertas.model.JobOffer;
import com.b2bmatch.ofertas.repository.JobApplicationRepository;
import com.b2bmatch.ofertas.repository.JobOfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobOfferRepository jobOfferRepository;

    public List<JobApplicationResponse> findAll() {
        return repository.findAll().stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public JobApplicationResponse findById(Long id, Long currentUserId, boolean isAdmin) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job application not found with id: " + id));
        requireOwner(entity, currentUserId, isAdmin);
        return JobApplicationResponse.fromEntity(entity);
    }

    public List<JobApplicationResponse> findByJobOfferId(Long jobOfferId, Long currentUserId, boolean isAdmin) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + jobOfferId));
        if (!isAdmin && !jobOffer.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo la empresa dueña de la oferta puede ver sus postulaciones");
        }
        return repository.findByJobOfferId(jobOfferId).stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public List<JobApplicationResponse> findByProfessionalId(Long professionalId, Long currentUserId, boolean isAdmin) {
        List<JobApplicationResponse> applications = repository.findByProfessionalId(professionalId).stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
        if (!isAdmin) {
            boolean allOwn = applications.stream().allMatch(app -> app.getUserId() != null && app.getUserId().equals(currentUserId));
            if (!allOwn) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes ver tus propias postulaciones");
            }
        }
        return applications;
    }

    public JobApplicationResponse create(JobApplicationRequest request, Long currentUserId, boolean isAdmin) {
        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + request.getJobOfferId()));

        JobApplication entity = new JobApplication();
        entity.setJobOffer(jobOffer);
        entity.setProfessionalId(request.getProfessionalId());
        entity.setUserId(isAdmin && request.getUserId() != null ? request.getUserId() : currentUserId);
        entity.setProposal(request.getProposal());
        entity.setExpectedPrice(request.getExpectedPrice());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        } else {
            entity.setStatus("PENDING");
        }
        entity.setCreatedAt(LocalDateTime.now());
        return JobApplicationResponse.fromEntity(repository.save(entity));
    }

    public JobApplicationResponse update(Long id, JobApplicationRequest request, Long currentUserId, boolean isAdmin) {
        JobApplication existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job application not found with id: " + id));
        requireOwner(existing, currentUserId, isAdmin);

        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(() -> new RuntimeException("Job offer not found with id: " + request.getJobOfferId()));

        existing.setJobOffer(jobOffer);
        existing.setProfessionalId(request.getProfessionalId());
        existing.setProposal(request.getProposal());
        existing.setExpectedPrice(request.getExpectedPrice());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        return JobApplicationResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        JobApplication existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job application not found with id: " + id));
        requireOwner(existing, currentUserId, isAdmin);
        repository.delete(existing);
    }

    private void requireOwner(JobApplication application, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return;
        boolean isApplicant = application.getUserId() != null && application.getUserId().equals(currentUserId);
        boolean isOfferOwner = application.getJobOffer() != null
                && application.getJobOffer().getUserId() != null
                && application.getJobOffer().getUserId().equals(currentUserId);
        if (!isApplicant && !isOfferOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso sobre esta postulación");
        }
    }
}
