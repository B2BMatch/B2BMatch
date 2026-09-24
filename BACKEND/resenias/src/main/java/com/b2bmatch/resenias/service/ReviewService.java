package com.b2bmatch.resenias.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.b2bmatch.resenias.config.NotificationClient;
import com.b2bmatch.resenias.exception.ForbiddenException;
import com.b2bmatch.resenias.exception.ResourceNotFoundException;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationClient notificationClient;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    
    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review no encontrada"));
    }

    public List<Review> getReviewsByProfessional(Long professionalId) {
        return reviewRepository.findByProfessionalId(professionalId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Review createReview(Review review, Long requesterId, String requesterRole) {
        if (!List.of("CUSTOMER", "COMPANY", "ADMIN").contains(requesterRole)) {
            throw new ForbiddenException("Solo clientes y empresas pueden dejar reseñas");
        }

        if (review.getId() != null) {
            throw new IllegalArgumentException("No puedes indicar un id al crear una reseña");
        }

        review.setUserId(requesterId);

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("rating debe estar entre 1 y 5");
        }

        Long professionalUser = jdbcTemplate.query(
                "SELECT user_id FROM perfiles.professional_profile WHERE id = ? AND status <> 'DELETED'",
                rs -> rs.next() ? rs.getLong(1) : null, review.getProfessionalId());
        if (professionalUser == null) {
            throw new IllegalArgumentException("El profesional indicado no existe o no está activo");
        }
        if (professionalUser.equals(requesterId)) {
            throw new IllegalArgumentException("No puedes dejar una reseña sobre tu propio perfil");
        }
        if (!"ADMIN".equals(requesterRole) && !hasCompletedTransaction(requesterId, requesterRole, professionalUser)) {
            throw new IllegalArgumentException(
                    "Necesitas una transacción previa completada (postulación o cotización aceptada) para dejar una reseña");
        }
        if (reviewRepository.existsByUserIdAndProfessionalId(requesterId, review.getProfessionalId())) {
            throw new IllegalArgumentException("Ya dejaste una reseña para este profesional");
        }

        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        notificationClient.notify(professionalUser, "Recibiste una reseña",
                "Un usuario dejó una valoración (" + saved.getRating() + " estrellas) sobre tu perfil");
        return saved;
    }

    public Review updateReview(Long id, Review review, Long requesterId, String requesterRole) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review no encontrada para modificar"));

        checkOwnership(existingReview, requesterId, requesterRole);

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("rating debe estar entre 1 y 5");
        }

        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());
        existingReview.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(Long id, Long requesterId, String requesterRole) {
        Review review = getReview(id);
        checkOwnership(review, requesterId, requesterRole);
        reviewRepository.delete(review);
    }

    private boolean hasCompletedTransaction(Long requesterId, String requesterRole, Long professionalUserId) {
        org.springframework.jdbc.core.ResultSetExtractor<Boolean> toBoolean = rs -> rs.next() && rs.getBoolean(1);
        if ("COMPANY".equals(requesterRole)) {
            boolean hired = Boolean.TRUE.equals(jdbcTemplate.query(
                    "SELECT EXISTS(SELECT 1 FROM ofertas.application_table a "
                            + "JOIN ofertas.job_offer o ON o.id = a.job_offer_id "
                            + "WHERE a.user_id = ? AND o.user_id = ? AND a.status = 'ACCEPTED')",
                    toBoolean, professionalUserId, requesterId));
            boolean engagedByQuotation = Boolean.TRUE.equals(jdbcTemplate.query(
                    "SELECT EXISTS(SELECT 1 FROM ofertas.quotation q "
                            + "JOIN catalogo.professional_service ps ON ps.id = q.service_id "
                            + "JOIN perfiles.professional_profile pp ON pp.id = ps.professional_id "
                            + "WHERE q.user_id = ? AND pp.user_id = ? AND q.status = 'ACCEPTED')",
                    toBoolean, requesterId, professionalUserId));
            return hired || engagedByQuotation;
        }
        if ("CUSTOMER".equals(requesterRole)) {
            return Boolean.TRUE.equals(jdbcTemplate.query(
                    "SELECT EXISTS(SELECT 1 FROM ofertas.quotation q "
                            + "JOIN catalogo.professional_service ps ON ps.id = q.service_id "
                            + "JOIN perfiles.professional_profile pp ON pp.id = ps.professional_id "
                            + "WHERE q.user_id = ? AND pp.user_id = ? AND q.status = 'ACCEPTED')",
                    toBoolean, requesterId, professionalUserId));
        }
        return false;
    }

    private void checkOwnership(Review review, Long requesterId, String requesterRole) {
        boolean isOwner = review.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo el cliente que escribió la reseña puede modificarla, o ser ADMIN");
        }
    }
}
