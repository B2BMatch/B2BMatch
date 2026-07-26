package com.b2bmatch.resenias.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.b2bmatch.resenias.exception.ResourceNotFoundException;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    //ingles
    //mostrar todas las reviews de la lista
    public List<Review> getAllReviews(){
        return reviewRepository.findAll();
    }

    //ver una reseña por id
    public Review getReview(Long id) {

        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review no encontrada"));
        /* Review review = reviewRepository.findById(professional_id). */
    }

    //ver todas las reseñas de un profesioal
    public List<Review> getReviewsByProfessional(Long professional_id){
            List<Review> reviews = reviewRepository.findByProfessionalId(professional_id);

            if (reviews.isEmpty()){
                throw new ResourceNotFoundException("Profesional con id " + professional_id + " no encontrado dentro de entidad Review");
            }
            return reviews;
        }
    //reseñas de un cliente por id
    public List<Review> getReviewsByCustomer(Long customer_id){
        List<Review> reviews = reviewRepository.findByCustomerId(customer_id);
        
        if (reviews.isEmpty()) {
        throw new ResourceNotFoundException("Cliente con id " + customer_id + " no encontrado dentro de entidad Review");
        }
        return reviews;
    }

    //crear una review
    public Review createReview (Review review){
        return reviewRepository.save(review);
    }

    //actualizar review
    public Review updateReview (Long id, Review review){
        
        Review existingReview = reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review no encontrada para modificar"));

        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());

        return reviewRepository.save(existingReview);
    }

    //borrar review
    public void deleteReview (Long id){
        Review review = getReview(id); //primero verifica que exista la review
        reviewRepository.delete(review);
        //o tambien por la entidad en si reviewRepository.delete(review);
    }

}
