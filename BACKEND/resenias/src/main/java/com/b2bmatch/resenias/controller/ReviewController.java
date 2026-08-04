package com.b2bmatch.resenias.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.resenias.config.JwtService;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.service.ReviewService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtService jwtService;

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
    public List<Review> getReviewsByCustomer(@PathVariable("customerId") Long customerId) {
        return reviewService.getReviewsByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @PutMapping("/{id}")
    public Review updateReview(
            @PathVariable Long id,
            @RequestBody Review review,
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        return reviewService.updateReview(id, review, requesterId, requesterRole);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        reviewService.deleteReview(id, requesterId, requesterRole);
    }
}