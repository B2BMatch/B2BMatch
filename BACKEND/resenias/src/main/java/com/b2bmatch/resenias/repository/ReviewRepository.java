package com.b2bmatch.resenias.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b2bmatch.resenias.model.Review;

public interface ReviewRepository extends JpaRepository <Review, Long>{

    //en ingles para seguir con la logica anterior
    List<Review> findByProfessionalId(Long professional_id);

    List<Review> findByCustomerId(Long customerId);

}
