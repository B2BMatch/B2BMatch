package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.ofertas.dto.QuotationRequest;
import com.b2bmatch.ofertas.dto.QuotationResponse;
import com.b2bmatch.ofertas.model.Quotation;
import com.b2bmatch.ofertas.repository.QuotationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuotationService {

    private final QuotationRepository repository;

    public List<QuotationResponse> findAll() {
        return repository.findAll().stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public QuotationResponse findById(Long id, Long currentUserId, boolean isAdmin) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        requireOwner(entity.getCustomerId(), currentUserId, isAdmin);
        return QuotationResponse.fromEntity(entity);
    }

    public List<QuotationResponse> findByServiceId(Long serviceId) {
        return repository.findByServiceId(serviceId).stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public List<QuotationResponse> findByCustomerId(Long customerId, Long currentUserId, boolean isAdmin) {
        if (!isAdmin && !customerId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes ver tus propias cotizaciones");
        }
        return repository.findByCustomerId(customerId).stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public QuotationResponse create(QuotationRequest request, Long currentUserId, boolean isAdmin) {
        Quotation entity = new Quotation();
        entity.setServiceId(request.getServiceId());
        entity.setCustomerId(isAdmin && request.getCustomerId() != null ? request.getCustomerId() : currentUserId);
        entity.setMessage(request.getMessage());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        } else {
            entity.setStatus("PENDING");
        }
        entity.setCreatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(entity));
    }

    public QuotationResponse update(Long id, QuotationRequest request, Long currentUserId, boolean isAdmin) {
        Quotation existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        requireOwner(existing.getCustomerId(), currentUserId, isAdmin);
        existing.setServiceId(request.getServiceId());
        existing.setMessage(request.getMessage());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Quotation existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        requireOwner(existing.getCustomerId(), currentUserId, isAdmin);
        repository.delete(existing);
    }

    private void requireOwner(Long ownerCustomerId, Long currentUserId, boolean isAdmin) {
        if (!isAdmin && !ownerCustomerId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el solicitante de la cotización puede modificarla");
        }
    }
}
