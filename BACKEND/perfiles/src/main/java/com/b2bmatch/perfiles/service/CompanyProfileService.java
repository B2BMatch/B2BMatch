package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.perfiles.dto.CompanyProfileRequest;
import com.b2bmatch.perfiles.dto.CompanyProfileResponse;
import com.b2bmatch.perfiles.model.CompanyProfile;
import com.b2bmatch.perfiles.repository.CompanyProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyProfileService {

    private final CompanyProfileRepository repository;

    public List<CompanyProfileResponse> findAll() {
        return repository.findAll().stream()
                .map(CompanyProfileResponse::fromEntity)
                .toList();
    }

    public CompanyProfileResponse findById(Long id) {
        CompanyProfile entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company profile not found with id: " + id));
        return CompanyProfileResponse.fromEntity(entity);
    }

    public CompanyProfileResponse findByUserId(Long userId) {
        CompanyProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Company profile not found for user id: " + userId));
        return CompanyProfileResponse.fromEntity(entity);
    }

    public CompanyProfileResponse create(CompanyProfileRequest request) {
        CompanyProfile entity = new CompanyProfile();
        entity.setUserId(request.getUserId());
        entity.setCompanyName(request.getCompanyName());
        entity.setTaxId(request.getTaxId());
        entity.setIndustry(request.getIndustry());
        entity.setWebsite(request.getWebsite());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setCountry(request.getCountry());
        entity.setCompanyDescription(request.getCompanyDescription());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setCreatedAt(LocalDateTime.now());
        return CompanyProfileResponse.fromEntity(repository.save(entity));
    }

    public CompanyProfileResponse update(Long id, CompanyProfileRequest request) {
        CompanyProfile existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company profile not found with id: " + id));
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

    public void delete(Long id) {
        repository.deleteById(id);
    }

}
