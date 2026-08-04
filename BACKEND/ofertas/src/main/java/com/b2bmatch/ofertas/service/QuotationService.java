package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.ofertas.dto.QuotationRequest;
import com.b2bmatch.ofertas.dto.QuotationResponse;
import com.b2bmatch.ofertas.exception.ForbiddenException;
import com.b2bmatch.ofertas.exception.OfferNotFoundException;
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

    public QuotationResponse findById(Long id) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));
        return QuotationResponse.fromEntity(entity);
    }

    public List<QuotationResponse> findByServiceId(Long serviceId) {
        return repository.findByServiceId(serviceId).stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public List<QuotationResponse> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public QuotationResponse create(QuotationRequest request) {
        Quotation entity = new Quotation();
        entity.setServiceId(request.getServiceId());
        entity.setCustomerId(request.getCustomerId());
        entity.setMessage(request.getMessage());
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(entity));
    }

    public QuotationResponse update(Long id, QuotationRequest request) {
        Quotation existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));
        existing.setServiceId(request.getServiceId());
        existing.setCustomerId(request.getCustomerId());
        existing.setMessage(request.getMessage());
        existing.setUpdatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long requesterId, String requesterRole) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));

        boolean isOwner = entity.getCustomerId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo el cliente que solicitó la cotización puede eliminarla, o ser ADMIN");
        }

        repository.deleteById(id);
    }

    public QuotationResponse accept(Long id, String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            throw new ForbiddenException(
                    "Por ahora, solo ADMIN puede aceptar cotizaciones (pendiente validar dueño real del servicio)");
        }
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden aceptar cotizaciones en estado PENDING");
        }

        entity.setStatus("ACCEPTED");
        entity.setUpdatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(entity));
    }

    public QuotationResponse reject(Long id, String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            throw new ForbiddenException(
                    "Por ahora, solo ADMIN puede rechazar cotizaciones (pendiente validar dueño real del servicio)");
        }
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden rechazar cotizaciones en estado PENDING");
        }

        entity.setStatus("REJECTED");
        entity.setUpdatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(entity));
    }
}
