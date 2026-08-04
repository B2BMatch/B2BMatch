package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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

    public List<CustomerProfileResponse> findAll() {
        return repository.findByStatusNot("DELETED").stream()
                .map(CustomerProfileResponse::fromEntity)
                .toList();
    }

    public CustomerProfileResponse findById(Long id) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));
        return CustomerProfileResponse.fromEntity(entity);
    }

    public CustomerProfileResponse findByUserId(Long userId) {
        CustomerProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found for user id: " + userId));
        return CustomerProfileResponse.fromEntity(entity);
    }

    public CustomerProfileResponse create(CustomerProfileRequest request) {
        CustomerProfile entity = new CustomerProfile();
        entity.setUserId(request.getUserId());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setCountry(request.getCountry());
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        return CustomerProfileResponse.fromEntity(repository.save(entity));
    }

    public CustomerProfileResponse update(Long id, CustomerProfileRequest request) {
        CustomerProfile existing = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setCountry(request.getCountry());
        existing.setUpdatedAt(LocalDateTime.now());
        return CustomerProfileResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long requesterId, String requesterRole) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));

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

    public CustomerProfileResponse reactivate(Long id, Long requesterId, String requesterRole) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Customer profile not found with id: " + id));

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
        return CustomerProfileResponse.fromEntity(repository.save(entity));
    }
}