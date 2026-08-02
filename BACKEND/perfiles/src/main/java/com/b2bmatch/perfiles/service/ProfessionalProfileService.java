package com.b2bmatch.perfiles.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.perfiles.dto.ProfessionalProfileRequest;
import com.b2bmatch.perfiles.dto.ProfessionalProfileResponse;
import com.b2bmatch.perfiles.model.ProfessionalProfile;
import com.b2bmatch.perfiles.repository.ProfessionalProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository repository;

    public List<ProfessionalProfileResponse> findAll() {
        return repository.findAll().stream()
                .map(ProfessionalProfileResponse::fromEntity)
                .toList();
    }

    public ProfessionalProfileResponse findById(Long id) {
        ProfessionalProfile entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professional profile not found with id: " + id));
        return ProfessionalProfileResponse.fromEntity(entity);
    }

    public ProfessionalProfileResponse findByUserId(Long userId) {
        ProfessionalProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Professional profile not found for user id: " + userId));
        return ProfessionalProfileResponse.fromEntity(entity);
    }

    public ProfessionalProfileResponse create(ProfessionalProfileRequest request) {
        ProfessionalProfile entity = new ProfessionalProfile();
        entity.setUserId(request.getUserId());
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
        entity.setCreatedAt(LocalDateTime.now());
        return ProfessionalProfileResponse.fromEntity(repository.save(entity));
    }

    public ProfessionalProfileResponse update(Long id, ProfessionalProfileRequest request) {
        ProfessionalProfile existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professional profile not found with id: " + id));
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

    public void delete(Long id) {
        repository.deleteById(id);
    }

}
