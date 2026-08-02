package com.b2bmatch.resenias.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private void requireAuthor(Authentication authentication, Long reviewCustomerId) {
        if (!isAdmin(authentication) && !reviewCustomerId.equals(currentUserId(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor de la reseña puede modificarla");
        }
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @GetMapping("/professional/{professionalId}")
    public List<Review> getReviewsByProfessional(@PathVariable("professionalId") Long professionalId) {
        return reviewService.getReviewsByProfessional(professionalId);
    }

    @GetMapping("/customer/{customerId}")
    public List<Review> getReviewsByCustomer(@PathVariable("customerId") Long customerId, Authentication authentication) {
        if (!isAdmin(authentication) && !customerId.equals(currentUserId(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes ver tus propias reseñas");
        }
        return reviewService.getReviewsByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@RequestBody Review review, Authentication authentication) {
        if (!isAdmin(authentication)) {
            review.setCustomerId(currentUserId(authentication));
        }
        return reviewService.createReview(review);
    }

    @PutMapping("/{id}")
    public Review updateReview(@PathVariable Long id, @RequestBody Review review, Authentication authentication) {
        Review existing = reviewService.getReview(id);
        requireAuthor(authentication, existing.getCustomerId());
        return reviewService.updateReview(id, review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id, Authentication authentication) {
        Review existing = reviewService.getReview(id);
        requireAuthor(authentication, existing.getCustomerId());
        reviewService.deleteReview(id);
    }
}
