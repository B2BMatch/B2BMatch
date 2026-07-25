package com.b2bmatch.resenias.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    //ingles
    //ver una reseña por id
    public Review getReview(Long id) {

        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review no encontrada"));
        /* Review review = reviewRepository.findById(professional_id). */
    }

    //ver todas las reseñas de un profesioal
    public List<Review> getReviewsByProfessional(Long professional_id){
            return reviewRepository.findByProfessionalId(professional_id);
        }
    //reseñas de un cliente por id
    public List<Review> getReviewsByCustomer(Long customer_id){
        return reviewRepository.findByCustomerId(customer_id);
    }

    //crear una review
    public Review createReview (Review review){
        reviewRepository.save(review);
        return review;
    }

    public Review updateReview (Long id, Review review){
        
        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review no encontrada para modificar"));
        review.setRating(0);
        review.setComment(null);
        reviewRepository.save(review)
    }

    public void deletReview (Long id){
        reviewRepository.deleteById(id);
        //o tambien por la entidad en si reviewRepository.delete(review);
    }

}
