package com.b2bmatch.resenias.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.resenias.model.Review;
import com.b2bmatch.resenias.service.ReviewService;

import lombok.RequiredArgsConstructor;

/* Controller
Lo dejaría para el final. Cuando llegues aquí ya tendrás todo listo:
entidad
repositorio
DTOs
servicio
excepciones
Entonces el controlador solo se dedica a exponer los endpoints:

POST   /reviews
GET    /reviews
GET    /reviews/{id}
PUT    /reviews/{id}
DELETE /reviews/{id}
y delega la lógica al servicio. */

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    //logica de "negocio", todos los metodos que cree en service iterados a partir de acá dandoles una ruta.
    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping ("/{id}")
    public Review getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    //por profesional
    @GetMapping("/professional/{professional_id}")
    public List<Review> getReviewsByProfessional(@PathVariable Long professional_id) {
        return reviewService.getReviewsByProfessional(professional_id);
    }

    //por cliente
    @GetMapping("/customer/{customer_id}")
    public List<Review> getReviewsByCustomer(@PathVariable Long customer_id) {
        return reviewService.getReviewsByCustomer(customer_id);
    }

    //publicar review
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //para dar un codigo 201 "creado" en vez de 200 "ok"
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    //modificar review existente
    @PutMapping("/{id}")
    public Review updateReview(@PathVariable Long id, @RequestBody Review review) {
        return reviewService.updateReview(id, review);
    }

    //borrar
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}
