package com.b2bmatch.resenias.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.resenias.exception.ForbiddenException;
import com.b2bmatch.resenias.exception.ResourceNotFoundException;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

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

    public List<Review> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    public Review createReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public Review updateReview(Long id, Review review, Long requesterId, String requesterRole) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review no encontrada para modificar"));

        checkOwnership(existingReview, requesterId, requesterRole);

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

    private void checkOwnership(Review review, Long requesterId, String requesterRole) {
        boolean isOwner = review.getCustomerId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Solo el cliente que escribió la reseña puede modificarla, o ser ADMIN");
        }
    }
}