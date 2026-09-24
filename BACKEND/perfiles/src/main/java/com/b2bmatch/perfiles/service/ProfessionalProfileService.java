package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.perfiles.dto.ProfessionalProfileRequest;
import com.b2bmatch.perfiles.dto.ProfessionalProfileResponse;
import com.b2bmatch.perfiles.exception.ForbiddenException;
import com.b2bmatch.perfiles.exception.ProfileNotFoundException;
import com.b2bmatch.perfiles.model.ProfessionalProfile;
import com.b2bmatch.perfiles.repository.ProfessionalProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository repository;

    public List<ProfessionalProfileResponse> findAll(Long requesterId, String requesterRole) {
        boolean full = isFullAccess(null, requesterId, requesterRole);
        return repository.findByStatusNot("DELETED").stream()
                .map(ProfessionalProfileResponse::fromEntity)
                .map(dto -> full ? dto : mask(dto))
                .toList();
    }

    public ProfessionalProfileResponse findById(Long id, Long requesterId, String requesterRole) {
        ProfessionalProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Professional profile not found with id: " + id));
        if ("DELETED".equals(entity.getStatus())) {
            throw new ProfileNotFoundException("Professional profile not found with id: " + id);
        }
        return toResponse(entity, requesterId, requesterRole);
    }

    public ProfessionalProfileResponse findByUserId(Long userId, Long requesterId, String requesterRole) {
        ProfessionalProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Professional profile not found for user id: " + userId));
        if ("DELETED".equals(entity.getStatus())) {
            throw new ProfileNotFoundException("Professional profile not found for user id: " + userId);
        }
        return toResponse(entity, requesterId, requesterRole);
    }

    private boolean isFullAccess(ProfessionalProfile entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return true;
        }
        return entity != null && requesterId != null && entity.getUserId().equals(requesterId);
    }

    private ProfessionalProfileResponse toResponse(ProfessionalProfile entity, Long requesterId, String requesterRole) {
        ProfessionalProfileResponse dto = ProfessionalProfileResponse.fromEntity(entity);
        return isFullAccess(entity, requesterId, requesterRole) ? dto : mask(dto);
    }

    private ProfessionalProfileResponse mask(ProfessionalProfileResponse dto) {
        dto.setUserId(null);
        dto.setPhone(null);
        dto.setPortfolioUrl(null);
        dto.setLinkedinUrl(null);
        dto.setGithubUrl(null);
        return dto;
    }

    @Transactional
    public ProfessionalProfileResponse create(ProfessionalProfileRequest request) {
        if (repository.findByUserId(request.getUserId())
                .map(ProfessionalProfile::getStatus)
                .filter("ACTIVE"::equals)
                .isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene un perfil profesional registrado");
        }
        ProfessionalProfile entity = repository.findByUserId(request.getUserId()).orElse(null);
        if (entity == null) {
            entity = new ProfessionalProfile();
            entity.setUserId(request.getUserId());
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setPhone(request.getPhone());
        entity.setBiography(request.getBiography());
        entity.setExperienceYears(request.getExperienceYears());
        entity.setHourlyRate(request.getHourlyRate());
        entity.setPortfolioUrl(request.getPortfolioUrl());
        entity.setLinkedinUrl(request.getLinkedinUrl());
        entity.setGithubUrl(request.getGithubUrl());
        entity.setCity(request.getCity());
        entity.setCountry(request.getCountry());
        entity.setStatus("ACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        return ProfessionalProfileResponse.fromEntity(repository.save(entity));
    }

    @Transactional
    public ProfessionalProfileResponse update(Long id, ProfessionalProfileRequest request,
            Long requesterId, String requesterRole) {
        ProfessionalProfile existing = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Professional profile not found with id: " + id));

        boolean isOwner = existing.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes modificar tu propio perfil, o ser ADMIN");
        }

        if ("DELETED".equals(existing.getStatus())) {
            throw new IllegalArgumentException("El perfil está eliminado, no se puede modificar");
        }

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setPhone(request.getPhone());
        existing.setBiography(request.getBiography());
        existing.setExperienceYears(request.getExperienceYears());
        existing.setHourlyRate(request.getHourlyRate());
        existing.setPortfolioUrl(request.getPortfolioUrl());
        existing.setLinkedinUrl(request.getLinkedinUrl());
        existing.setGithubUrl(request.getGithubUrl());
        existing.setCity(request.getCity());
        existing.setCountry(request.getCountry());
        existing.setUpdatedAt(LocalDateTime.now());
        return ProfessionalProfileResponse.fromEntity(repository.save(existing));
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        ProfessionalProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Professional profile not found with id: " + id));

        boolean isOwner = entity.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes eliminar tu propio perfil, o ser ADMIN");
        }

        if ("DELETED".equals(entity.getStatus())) {
            throw new IllegalArgumentException("El perfil ya está eliminado");
        }

        entity.setStatus("DELETED");
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Transactional
    public ProfessionalProfileResponse reactivate(Long id, Long requesterId, String requesterRole) {
        ProfessionalProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Professional profile not found with id: " + id));

        boolean isOwner = entity.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes reactivar tu propio perfil, o ser ADMIN");
        }

        if (!"DELETED".equals(entity.getStatus())) {
            throw new IllegalArgumentException("El perfil no está eliminado, no se puede reactivar");
        }

        entity.setStatus("ACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        return ProfessionalProfileResponse.fromEntity(repository.save(entity));
    }
}
