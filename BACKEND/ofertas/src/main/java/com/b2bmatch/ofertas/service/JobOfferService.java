package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.ofertas.dto.JobOfferRequest;
import com.b2bmatch.ofertas.dto.JobOfferResponse;
import com.b2bmatch.ofertas.exception.ForbiddenException;
import com.b2bmatch.ofertas.exception.OfferNotFoundException;
import com.b2bmatch.ofertas.model.JobOffer;
import com.b2bmatch.ofertas.repository.JobOfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferRepository repository;
    private final ProfileOwnerResolver profileOwnerResolver;

    public List<JobOfferResponse> findAll() {
        return repository.findByDeletedAtIsNull().stream()
                .filter(offer -> !List.of("CLOSED", "EXPIRED").contains(offer.getStatus()))
                .map(JobOfferResponse::fromEntity)
                .toList();
    }

    public JobOfferResponse findById(Long id) {
        JobOffer entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + id));
        if (entity.getDeletedAt() != null) {
            throw new OfferNotFoundException("Job offer not found with id: " + id);
        }
        return JobOfferResponse.fromEntity(entity);
    }

    public List<JobOfferResponse> findByUserId(Long userId) {
        return repository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(JobOfferResponse::fromEntity)
                .toList();
    }

    public JobOfferResponse create(JobOfferRequest request, Long requesterId, String requesterRole) {
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!"COMPANY".equals(requesterRole) && !isAdmin) {
            throw new ForbiddenException("Solo las empresas pueden publicar ofertas de empleo");
        }

        if (!isAdmin && !profileOwnerResolver.hasActiveCompanyProfile(requesterId)) {
            throw new IllegalArgumentException("La empresa indicada no existe o no está activa");
        }

        if (!profileOwnerResolver.hasActiveCategory(request.getCategoryId())) {
            throw new IllegalArgumentException("La categoría indicada no existe o no está activa");
        }

        JobOffer entity = new JobOffer();
        entity.setUserId(requesterId);
        entity.setCategoryId(request.getCategoryId());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setBudget(request.getBudget());
        entity.setDeadline(request.getDeadline());
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(repository.save(entity));
    }

    public JobOfferResponse update(Long id, JobOfferRequest request, Long requesterId, String requesterRole) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + id));

        if (existing.getDeletedAt() != null) {
            throw new OfferNotFoundException("Job offer not found with id: " + id);
        }

        checkOwnership(existing, requesterId, requesterRole);

        if (!profileOwnerResolver.hasActiveCategory(request.getCategoryId())) {
            throw new IllegalArgumentException("La categoría indicada no existe o no está activa");
        }

        existing.setCategoryId(request.getCategoryId());
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setBudget(request.getBudget());
        existing.setDeadline(request.getDeadline());
        existing.setUpdatedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(repository.save(existing));
    }

    public JobOfferResponse updateStatus(Long id, String status, Long requesterId, String requesterRole) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + id));

        // Sin guard de "borrada" aqui a proposito: este es el unico camino para
        // reactivar una oferta eliminada, y el guard lo hacia inalcanzable. El
        // permiso lo resuelve el chequeo de ADMIN mas abajo.
        checkOwnership(existing, requesterId, requesterRole);

        String newStatus = status.toUpperCase();
        if (!List.of("ACTIVE", "INACTIVE", "SUSPENDED", "CLOSED", "EXPIRED").contains(newStatus)) {
            throw new IllegalArgumentException("Estado no válido");
        }

        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isAdmin && ("CLOSED".equals(existing.getStatus()) || "EXPIRED".equals(existing.getStatus()))) {
            throw new ForbiddenException("La oferta está cerrada o vencida y no puede cambiar de estado");
        }

        if ("SUSPENDED".equals(newStatus) && !isAdmin) {
            throw new ForbiddenException("Solo ADMIN puede suspender una oferta");
        }

        if ("SUSPENDED".equals(existing.getStatus()) && !"SUSPENDED".equals(newStatus) && !isAdmin) {
            throw new ForbiddenException("Solo ADMIN puede des-suspender una oferta");
        }

        if (existing.getDeletedAt() != null && !isAdmin) {
            throw new ForbiddenException("Solo ADMIN puede reactivar una oferta eliminada");
        }

        // `status` es estado de negocio y el borrado vive en deleted_at, asi que
        // este endpoint ya no mezcla las dos cosas: cambiar de estado restaura
        // la oferta si estaba dada de baja, sin inventarse un estado previo.
        if (existing.getDeletedAt() != null) {
            existing.setDeletedAt(null);
        }
        existing.setStatus(newStatus);
        existing.setUpdatedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long requesterId, String requesterRole) {
        JobOffer existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Job offer not found with id: " + id));

        if (existing.getDeletedAt() != null) {
            throw new OfferNotFoundException("Job offer not found with id: " + id);
        }

        checkOwnership(existing, requesterId, requesterRole);

        // No se toca `status`: es estado de negocio y el borrado vive en deleted_at.
        existing.setDeletedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }

    private void checkOwnership(JobOffer offer, Long requesterId, String requesterRole) {
        boolean isAdmin = "ADMIN".equals(requesterRole);
        boolean isOwner = offer.getUserId().equals(requesterId);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo el dueño de la oferta puede realizar esta acción, o ser ADMIN");
        }
    }
}
