package com.b2bmatch.ofertas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.ofertas.config.NotificationClient;
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
    private final ProfileOwnerResolver profileOwnerResolver;
    private final NotificationClient notificationClient;

    public List<QuotationResponse> findAll(String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            throw new ForbiddenException("Solo ADMIN puede listar todas las cotizaciones");
        }
        return repository.findAll().stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public QuotationResponse findById(Long id, Long requesterId, String requesterRole) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));
        checkQuotationReadAccess(entity, requesterId, requesterRole);
        return QuotationResponse.fromEntity(entity);
    }

    public List<QuotationResponse> findByServiceId(Long serviceId, Long requesterId, String requesterRole) {
        if (!"ADMIN".equals(requesterRole)) {
            Long ownerUser = profileOwnerResolver.serviceOwnerUserId(serviceId);
            if (ownerUser == null || !ownerUser.equals(requesterId)) {
                throw new ForbiddenException(
                        "Solo el profesional dueño del servicio puede ver sus cotizaciones, o ser ADMIN");
            }
        }
        return repository.findByServiceId(serviceId).stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public List<QuotationResponse> findByUserId(Long userId, Long requesterId, String requesterRole) {
        if (!"ADMIN".equals(requesterRole) && !userId.equals(requesterId)) {
            throw new ForbiddenException("Solo puedes ver tus propias cotizaciones, o ser ADMIN");
        }
        return repository.findByUserId(userId).stream()
                .map(QuotationResponse::fromEntity)
                .toList();
    }

    public QuotationResponse create(QuotationRequest request, Long requesterId, String requesterRole) {
        Long ownerUser = profileOwnerResolver.serviceOwnerUserId(request.getServiceId());
        if (ownerUser == null || !profileOwnerResolver.isActiveService(request.getServiceId())) {
            throw new IllegalArgumentException("El servicio indicado no existe o no está disponible");
        }

        if (!"ADMIN".equals(requesterRole) && ownerUser.equals(requesterId)) {
            throw new ForbiddenException("No puedes solicitar una cotización sobre tu propio servicio");
        }

        Quotation entity = new Quotation();
        entity.setServiceId(request.getServiceId());
        entity.setUserId(requesterId);
        entity.setMessage(request.getMessage());
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        Quotation saved = repository.save(entity);
        notificationClient.notify(ownerUser, "Nueva cotización",
                "Recibiste una solicitud de cotización para tu servicio con id " + request.getServiceId());
        return QuotationResponse.fromEntity(saved);
    }

    public QuotationResponse update(Long id, QuotationRequest request, Long requesterId, String requesterRole) {
        Quotation existing = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));

        if (!existing.getUserId().equals(requesterId) && !"ADMIN".equals(requesterRole)) {
            throw new ForbiddenException("Solo el cliente que solicitó la cotización puede editarla, o ser ADMIN");
        }

        if (!"PENDING".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden editar cotizaciones en estado PENDING");
        }

        Long ownerUser = profileOwnerResolver.serviceOwnerUserId(request.getServiceId());
        if (ownerUser == null || !profileOwnerResolver.isActiveService(request.getServiceId())) {
            throw new IllegalArgumentException("El servicio indicado no existe o no está disponible");
        }

        existing.setServiceId(request.getServiceId());
        existing.setMessage(request.getMessage());
        existing.setUpdatedAt(LocalDateTime.now());
        return QuotationResponse.fromEntity(repository.save(existing));
    }

    public void delete(Long id, Long requesterId, String requesterRole) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));

        Long ownerUser = profileOwnerResolver.serviceOwnerUserId(entity.getServiceId());
        boolean isCustomer = entity.getUserId().equals(requesterId);
        boolean isServiceOwner = ownerUser != null && ownerUser.equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isCustomer && !isServiceOwner && !isAdmin) {
            throw new ForbiddenException(
                    "Solo el cliente que solicitó la cotización puede eliminarla, o ser ADMIN");
        }

        repository.deleteById(id);
    }

    public QuotationResponse accept(Long id, Long requesterId, String requesterRole) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));
        checkServiceOwner(entity, requesterId, requesterRole);

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden aceptar cotizaciones en estado PENDING");
        }

        entity.setStatus("ACCEPTED");
        entity.setUpdatedAt(LocalDateTime.now());
        QuotationResponse response = QuotationResponse.fromEntity(repository.save(entity));
        notificationClient.notify(entity.getUserId(), "Cotización aceptada",
                "El profesional aceptó tu solicitud de cotización para el servicio con id " + entity.getServiceId());
        return response;
    }

    public QuotationResponse reject(Long id, Long requesterId, String requesterRole) {
        Quotation entity = repository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Quotation not found with id: " + id));
        checkServiceOwner(entity, requesterId, requesterRole);

        if (!"PENDING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden rechazar cotizaciones en estado PENDING");
        }

        entity.setStatus("REJECTED");
        entity.setUpdatedAt(LocalDateTime.now());
        QuotationResponse response = QuotationResponse.fromEntity(repository.save(entity));
        notificationClient.notify(entity.getUserId(), "Cotización rechazada",
                "El profesional rechazó tu solicitud de cotización para el servicio con id " + entity.getServiceId());
        return response;
    }

    private void checkQuotationReadAccess(Quotation entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return;
        }
        Long ownerUser = profileOwnerResolver.serviceOwnerUserId(entity.getServiceId());
        boolean isCustomer = entity.getUserId().equals(requesterId);
        boolean isServiceOwner = ownerUser != null && ownerUser.equals(requesterId);
        if (!isCustomer && !isServiceOwner) {
            throw new ForbiddenException("No tienes acceso a esta cotización");
        }
    }

    private void checkServiceOwner(Quotation entity, Long requesterId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return;
        }
        Long ownerUser = profileOwnerResolver.serviceOwnerUserId(entity.getServiceId());
        boolean isOwner = ownerUser != null && ownerUser.equals(requesterId);
        if (!isOwner) {
            throw new ForbiddenException(
                    "Solo el profesional dueño del servicio puede aceptar/rechazar cotizaciones, o ser ADMIN");
        }
    }
}
