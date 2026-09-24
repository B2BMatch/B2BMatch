package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.ofertas.config.NotificationClient;
import com.b2bmatch.ofertas.dto.JobApplicationRequest;
import com.b2bmatch.ofertas.dto.JobApplicationResponse;
import com.b2bmatch.ofertas.exception.ForbiddenException;
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
    private final ProfileOwnerResolver profileOwnerResolver;
    private final NotificationClient notificationClient;

    public List<JobApplicationResponse> findAll(String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            throw new ForbiddenException("Solo ADMIN puede listar todas las postulaciones");
        }
        return repository.findAll().stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public JobApplicationResponse findById(Long id, Long requesterId, String requesterRole) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));
        checkApplicationReadAccess(entity, requesterId, requesterRole);
        return JobApplicationResponse.fromEntity(entity);
    }

    public List<JobApplicationResponse> findByJobOfferId(Long jobOfferId, Long requesterId, String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            JobOffer offer = jobOfferRepository.findById(jobOfferId)
                    .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + jobOfferId));
            if (!offer.getUserId().equals(requesterId)) {
                throw new ForbiddenException("Solo la empresa dueña de la oferta puede ver sus postulaciones, o ser ADMIN");
            }
        }
        return repository.findByJobOfferId(jobOfferId).stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public List<JobApplicationResponse> findByUserId(Long userId, Long requesterId, String requesterRole) {
        if (!"ADMIN".equals(requesterRole) && !userId.equals(requesterId)) {
            throw new ForbiddenException("Solo puedes ver tus propias postulaciones, o ser ADMIN");
        }
        return repository.findByUserId(userId).stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    public JobApplicationResponse create(JobApplicationRequest request, Long requesterId, String requesterRole) {
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!"PROFESSIONAL".equals(requesterRole) && !isAdmin) {
            throw new ForbiddenException("Solo los profesionales pueden postularse a las ofertas");
        }

        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(
                        () -> new OfferNotFoundException("Job offer not found with id: " + request.getJobOfferId()));

        if (!"ACTIVE".equals(jobOffer.getStatus())) {
            throw new IllegalArgumentException("La oferta no está disponible para postularse");
        }

        if (!isAdmin && !profileOwnerResolver.hasActiveProfessionalProfile(requesterId)) {
            throw new IllegalArgumentException("El perfil profesional indicado no existe o no está activo");
        }

        if (!isAdmin && jobOffer.getUserId().equals(requesterId)) {
            throw new ForbiddenException("No puedes postularte a una oferta de tu propia empresa");
        }

        JobApplication entity = new JobApplication();
        entity.setJobOffer(jobOffer);
        entity.setUserId(requesterId);
        entity.setProposal(request.getProposal());
        entity.setExpectedPrice(request.getExpectedPrice());
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        JobApplication saved = repository.save(entity);
        notificationClient.notify(jobOffer.getUserId(), "Nueva postulación",
                "Un profesional se postuló a tu oferta \"" + jobOffer.getTitle() + "\"");
        return JobApplicationResponse.fromEntity(saved);
    }

    public JobApplicationResponse update(Long id, JobApplicationRequest request, Long requesterId, String requesterRole) {
        JobApplication existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        checkApplicationReadAccess(existing, requesterId, requesterRole);

        if (!"PENDING".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden editar postulaciones en estado PENDING");
        }

        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(
                        () -> new OfferNotFoundException("Job offer not found with id: " + request.getJobOfferId()));

        if (!request.getJobOfferId().equals(existing.getJobOffer().getId())) {
            throw new IllegalArgumentException("No puedes mover la postulación a otra oferta");
        }

        if (!"ACTIVE".equals(jobOffer.getStatus())) {
            throw new IllegalArgumentException("La oferta no está disponible para postularse");
        }

        if (!"ADMIN".equals(requesterRole) && !existing.getUserId().equals(requesterId)) {
            throw new ForbiddenException("Solo puedes modificar postulaciones con tu propio perfil profesional");
        }

        existing.setProposal(request.getProposal());
        existing.setExpectedPrice(request.getExpectedPrice());
        existing.setUpdatedAt(LocalDateTime.now());
        return JobApplicationResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long requesterId, String requesterRole) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        boolean isOwner = entity.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    "Solo el profesional que postuló puede eliminar esta postulación, o ser ADMIN");
        }

        repository.deleteById(id);
    }

    @Transactional
    public JobApplicationResponse accept(Long id, Long requesterId, String requesterRole) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        JobOffer jobOffer = jobOfferRepository.findByIdForUpdate(entity.getJobOffer().getId())
                .orElseThrow(() -> new OfferNotFoundException(
                        "Job offer not found with id: " + entity.getJobOffer().getId()));

        if (!"ACTIVE".equals(jobOffer.getStatus())) {
            throw new IllegalArgumentException("La oferta ya no está activa, no se puede aceptar la postulación");
        }

        checkCompanyOwnership(entity, requesterId, requesterRole);

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden aceptar postulaciones en estado PENDING");
        }

        entity.setStatus("ACCEPTED");
        entity.setUpdatedAt(LocalDateTime.now());
        JobApplicationResponse response = JobApplicationResponse.fromEntity(repository.save(entity));

        List<JobApplication> otherApplications = repository.findByJobOfferId(jobOffer.getId());
        for (JobApplication other : otherApplications) {
            if (!other.getId().equals(entity.getId()) && "PENDING".equals(other.getStatus())) {
                other.setStatus("REJECTED");
                other.setUpdatedAt(LocalDateTime.now());
                repository.save(other);
            }
        }

        jobOffer.setStatus("CLOSED");
        jobOffer.setUpdatedAt(LocalDateTime.now());
        jobOfferRepository.save(jobOffer);

        notificationClient.notify(entity.getUserId(), "Postulación aceptada",
                "Tu postulación fue aceptada en la oferta \"" + jobOffer.getTitle() + "\". La oferta quedó cerrada.");

        return response;
    }

    public JobApplicationResponse reject(Long id, Long requesterId, String requesterRole) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job application not found with id: " + id));

        checkCompanyOwnership(entity, requesterId, requesterRole);

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden rechazar postulaciones en estado PENDING");
        }

        entity.setStatus("REJECTED");
        entity.setUpdatedAt(LocalDateTime.now());
        JobApplicationResponse response = JobApplicationResponse.fromEntity(repository.save(entity));
        notificationClient.notify(entity.getUserId(), "Postulación rechazada",
                "Tu postulación fue rechazada por la empresa propietaria de la oferta");
        return response;
    }

    private void checkCompanyOwnership(JobApplication entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return;
        }
        boolean isOwnerCompany = entity.getJobOffer().getUserId().equals(requesterId);
        if (!isOwnerCompany) {
            throw new ForbiddenException(
                    "Solo la empresa dueña de la oferta puede aceptar/rechazar postulaciones, o ser ADMIN");
        }
    }

    private void checkApplicationReadAccess(JobApplication entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return;
        }
        boolean isApplicant = entity.getUserId().equals(requesterId);
        boolean isCompany = entity.getJobOffer().getUserId().equals(requesterId);
        if (!isApplicant && !isCompany) {
            throw new ForbiddenException("No tienes acceso a esta postulación");
        }
    }
}
