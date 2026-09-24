package com.b2bmatch.resenias.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.resenias.config.JwtService;
import com.b2bmatch.resenias.dto.ReviewCreateRequestDto;
import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.service.ReviewService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
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

    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable("userId") Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    @GetMapping("/me")
    public List<Review> getMyReviews(@RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long userId = claims.get("userId", Long.class);
        return reviewService.getReviewsByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@Valid @RequestBody ReviewCreateRequestDto dto,
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = jwtService.parseToken(authHeader.substring(7));
        Long requesterId = claims.get("userId", Long.class);
        String requesterRole = claims.get("role", String.class);

        Review review = Review.builder()
                .professionalId(dto.getProfessionalId())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        return reviewService.createReview(review, requesterId, requesterRole);
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
