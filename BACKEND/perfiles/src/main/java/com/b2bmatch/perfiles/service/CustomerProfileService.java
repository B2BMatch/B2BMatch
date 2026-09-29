package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.perfiles.dto.CustomerProfileRequest;
import com.b2bmatch.perfiles.dto.CustomerProfileResponse;
import com.b2bmatch.perfiles.exception.ForbiddenException;
import com.b2bmatch.perfiles.exception.ProfileNotFoundException;
import com.b2bmatch.perfiles.model.CustomerProfile;
import com.b2bmatch.perfiles.repository.CustomerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository repository;

    public List<CustomerProfileResponse> findAll(Long requesterId, String requesterRole) {
        boolean full = isFullAccess(null, requesterId, requesterRole);
        return repository.findByDeletedAtIsNull().stream()
                .map(CustomerProfileResponse::fromEntity)
                .map(dto -> full ? dto : mask(dto))
                .toList();
    }

    public CustomerProfileResponse findById(Long id, Long requesterId, String requesterRole) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));
        if (entity.getDeletedAt() != null) {
            throw new ProfileNotFoundException("Customer profile not found with id: " + id);
        }
        return toResponse(entity, requesterId, requesterRole);
    }

    public CustomerProfileResponse findByUserId(Long userId, Long requesterId, String requesterRole) {
        CustomerProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found for user id: " + userId));
        if (entity.getDeletedAt() != null) {
            throw new ProfileNotFoundException("Customer profile not found for user id: " + userId);
        }
        return toResponse(entity, requesterId, requesterRole);
    }

    private boolean isFullAccess(CustomerProfile entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return true;
        }
        return entity != null && requesterId != null && entity.getUserId().equals(requesterId);
    }

    private CustomerProfileResponse toResponse(CustomerProfile entity, Long requesterId, String requesterRole) {
        CustomerProfileResponse dto = CustomerProfileResponse.fromEntity(entity);
        return isFullAccess(entity, requesterId, requesterRole) ? dto : mask(dto);
    }

    private CustomerProfileResponse mask(CustomerProfileResponse dto) {
        dto.setUserId(null);
        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setPhone(null);
        dto.setAddress(null);
        return dto;
    }

    @Transactional
    public CustomerProfileResponse create(CustomerProfileRequest request) {
        if (repository.findByUserId(request.getUserId())
                .map(CustomerProfile::getStatus)
                .filter("ACTIVE"::equals)
                .isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene un perfil de cliente registrado");
        }
        CustomerProfile entity = repository.findByUserId(request.getUserId()).orElse(null);
        if (entity == null) {
            entity = new CustomerProfile();
            entity.setUserId(request.getUserId());
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setCountry(request.getCountry());
        entity.setStatus("ACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        return CustomerProfileResponse.fromEntity(repository.save(entity));
    }

    @Transactional
    public CustomerProfileResponse update(Long id, CustomerProfileRequest request,
            Long requesterId, String requesterRole) {
        CustomerProfile existing = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));

        boolean isOwner = existing.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo puedes modificar tu propio perfil, o ser ADMIN");
        }

        if (existing.getDeletedAt() != null) {
            throw new IllegalArgumentException("El perfil está eliminado, no se puede modificar");
        }

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setCountry(request.getCountry());
        existing.setUpdatedAt(LocalDateTime.now());
        return CustomerProfileResponse.fromEntity(repository.save(existing));
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));

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
    public CustomerProfileResponse reactivate(Long id, Long requesterId, String requesterRole) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));

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
        return CustomerProfileResponse.fromEntity(repository.save(entity));
    }
}
