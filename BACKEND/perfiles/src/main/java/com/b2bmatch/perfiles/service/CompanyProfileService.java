package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.perfiles.dto.CompanyProfileRequest;
import com.b2bmatch.perfiles.dto.CompanyProfileResponse;
import com.b2bmatch.perfiles.exception.ForbiddenException;
import com.b2bmatch.perfiles.exception.ProfileNotFoundException;
import com.b2bmatch.perfiles.model.CompanyProfile;
import com.b2bmatch.perfiles.repository.CompanyProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private final CompanyProfileRepository repository;

    public List<CompanyProfileResponse> findAll(Long requesterId, String requesterRole) {
        boolean full = isFullAccess(null, requesterId, requesterRole);
        return repository.findByDeletedAtIsNull().stream()
                .map(CompanyProfileResponse::fromEntity)
                .map(dto -> full ? dto : mask(dto))
                .toList();
    }

    public CompanyProfileResponse findById(Long id, Long requesterId, String requesterRole) {
        CompanyProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Company profile not found with id: " + id));
        if (entity.getDeletedAt() != null) {
            throw new ProfileNotFoundException("Company profile not found with id: " + id);
        }
        return toResponse(entity, requesterId, requesterRole);
    }

    public CompanyProfileResponse findByUserId(Long userId, Long requesterId, String requesterRole) {
        CompanyProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Company profile not found for user id: " + userId));
        if (entity.getDeletedAt() != null) {
            throw new ProfileNotFoundException("Company profile not found for user id: " + userId);
        }
        return toResponse(entity, requesterId, requesterRole);
    }

    private boolean isFullAccess(CompanyProfile entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return true;
        }
        return entity != null && requesterId != null && entity.getUserId().equals(requesterId);
    }

    private CompanyProfileResponse toResponse(CompanyProfile entity, Long requesterId, String requesterRole) {
        CompanyProfileResponse dto = CompanyProfileResponse.fromEntity(entity);
        return isFullAccess(entity, requesterId, requesterRole) ? dto : mask(dto);
    }

    private CompanyProfileResponse mask(CompanyProfileResponse dto) {
        dto.setUserId(null);
        dto.setTaxId(null);
        dto.setEmail(null);
        dto.setPhone(null);
        dto.setAddress(null);
        return dto;
    }

    @Transactional
    public CompanyProfileResponse create(CompanyProfileRequest request) {
        CompanyProfile existing = repository.findByUserId(request.getUserId()).orElse(null);
        if (existing != null && "ACTIVE".equals(existing.getStatus())) {
            throw new IllegalArgumentException("El usuario ya tiene un perfil de empresa registrado");
        }
        CompanyProfile byTax = repository.findByTaxId(request.getTaxId()).orElse(null);
        if (byTax != null && (existing == null || !byTax.getId().equals(existing.getId()))) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con este tax id");
        }
        if (existing == null) {
            existing = new CompanyProfile();
            existing.setUserId(request.getUserId());
            existing.setCreatedAt(LocalDateTime.now());
        }
        existing.setCompanyName(request.getCompanyName());
        existing.setTaxId(request.getTaxId());
        existing.setIndustry(request.getIndustry());
        existing.setWebsite(request.getWebsite());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setCountry(request.getCountry());
        existing.setCompanyDescription(request.getCompanyDescription());
        existing.setLogoUrl(request.getLogoUrl());
        existing.setStatus("ACTIVE");
        existing.setUpdatedAt(LocalDateTime.now());
        return CompanyProfileResponse.fromEntity(repository.save(existing));
    }

    @Transactional
    public CompanyProfileResponse update(Long id, CompanyProfileRequest request,
            Long requesterId, String requesterRole) {
        CompanyProfile existing = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Company profile not found with id: " + id));

        boolean isOwner = existing.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes modificar tu propio perfil, o ser ADMIN");
        }

        if (existing.getDeletedAt() != null) {
            throw new IllegalArgumentException("El perfil está eliminado, no se puede modificar");
        }

        if (!existing.getTaxId().equals(request.getTaxId())
                && repository.findByTaxId(request.getTaxId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con este tax id");
        }

        existing.setCompanyName(request.getCompanyName());
        existing.setTaxId(request.getTaxId());
        existing.setIndustry(request.getIndustry());
        existing.setWebsite(request.getWebsite());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setCountry(request.getCountry());
        existing.setCompanyDescription(request.getCompanyDescription());
        existing.setLogoUrl(request.getLogoUrl());
        existing.setUpdatedAt(LocalDateTime.now());
        return CompanyProfileResponse.fromEntity(repository.save(existing));
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        CompanyProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Company profile not found with id: " + id));

        boolean isOwner = entity.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes eliminar tu propio perfil, o ser ADMIN");
        }

        if (entity.getDeletedAt() != null) {
            throw new IllegalArgumentException("El perfil ya está eliminado");
        }

        // No se toca `status`: es estado de negocio y el borrado vive en deleted_at.
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Transactional
    public CompanyProfileResponse reactivate(Long id, Long requesterId, String requesterRole) {
        CompanyProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Company profile not found with id: " + id));

        boolean isOwner = entity.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes reactivar tu propio perfil, o ser ADMIN");
        }

        if (entity.getDeletedAt() == null) {
            throw new IllegalArgumentException("El perfil no está eliminado, no se puede reactivar");
        }

        // `status` no se toco al dar de baja, asi que restaurar es solo limpiar
        // el borrado: no hay que adivinar ningun estado previo.
        entity.setDeletedAt(null);
        entity.setUpdatedAt(LocalDateTime.now());
        return CompanyProfileResponse.fromEntity(repository.save(entity));
    }
}
