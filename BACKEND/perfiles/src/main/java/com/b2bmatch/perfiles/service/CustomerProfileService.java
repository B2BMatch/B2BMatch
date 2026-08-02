package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.perfiles.dto.CustomerProfileRequest;
import com.b2bmatch.perfiles.dto.CustomerProfileResponse;
import com.b2bmatch.perfiles.model.CustomerProfile;
import com.b2bmatch.perfiles.repository.CustomerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository repository;

    public List<CustomerProfileResponse> findAll() {
        return repository.findAll().stream()
                .map(CustomerProfileResponse::fromEntity)
                .toList();
    }

    public CustomerProfileResponse findById(Long id) {
        CustomerProfile entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer profile not found with id: " + id));
        return CustomerProfileResponse.fromEntity(entity);
    }

    public CustomerProfileResponse findByUserId(Long userId) {
        CustomerProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found for user id: " + userId));
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
        entity.setCreatedAt(LocalDateTime.now());
        return CustomerProfileResponse.fromEntity(repository.save(entity));
    }

    public CustomerProfileResponse update(Long id, CustomerProfileRequest request) {
        CustomerProfile existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer profile not found with id: " + id));
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setCountry(request.getCountry());
        existing.setUpdatedAt(LocalDateTime.now());
        return CustomerProfileResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

}
