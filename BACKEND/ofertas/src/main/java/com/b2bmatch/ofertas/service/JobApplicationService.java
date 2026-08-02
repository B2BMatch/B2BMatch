package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.ofertas.dto.JobApplicationRequest;
import com.b2bmatch.ofertas.dto.JobApplicationResponse;
import com.b2bmatch.ofertas.exception.OfferNotFoundException;
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

    public JobApplicationResponse findById(Long id) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));
        return JobApplicationResponse.fromEntity(entity);
    }

    public List<JobApplicationResponse> findByJobOfferId(Long jobOfferId) {
        return repository.findByJobOfferId(jobOfferId).stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public List<JobApplicationResponse> findByProfessionalId(Long professionalId) {
        return repository.findByProfessionalId(professionalId).stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public JobApplicationResponse create(JobApplicationRequest request) {
        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + request.getJobOfferId()));

        JobApplication entity = new JobApplication();
        entity.setJobOffer(jobOffer);
        entity.setProfessionalId(request.getProfessionalId());
        entity.setProposal(request.getProposal());
        entity.setExpectedPrice(request.getExpectedPrice());
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        return JobApplicationResponse.fromEntity(repository.save(entity));
    }

    public JobApplicationResponse update(Long id, JobApplicationRequest request) {
        JobApplication existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + request.getJobOfferId()));

        existing.setJobOffer(jobOffer);
        existing.setProfessionalId(request.getProfessionalId());
        existing.setProposal(request.getProposal());
        existing.setExpectedPrice(request.getExpectedPrice());
        existing.setUpdatedAt(LocalDateTime.now());
        return JobApplicationResponse.fromEntity(repository.save(existing));
    }

    public JobApplicationResponse accept(Long id) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden aceptar postulaciones en estado PENDING");
        }

        entity.setStatus("ACCEPTED");
        entity.setUpdatedAt(LocalDateTime.now());
        JobApplicationResponse response = JobApplicationResponse.fromEntity(repository.save(entity));

        List<JobApplication> otherApplications = repository.findByJobOfferId(entity.getJobOffer().getId());
        for (JobApplication other : otherApplications) {
            if (!other.getId().equals(entity.getId()) && "PENDING".equals(other.getStatus())) {
                other.setStatus("REJECTED");
                other.setUpdatedAt(LocalDateTime.now());
                repository.save(other);
            }
        }

        return response;
    }

    public JobApplicationResponse reject(Long id) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden rechazar postulaciones en estado PENDING");
        }

        entity.setStatus("REJECTED");
        entity.setUpdatedAt(LocalDateTime.now());
        return JobApplicationResponse.fromEntity(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}