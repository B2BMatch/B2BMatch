package com.b2bmatch.ofertas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.b2bmatch.ofertas.model.JobOffer;

import jakarta.persistence.LockModeType;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByUserIdAndDeletedAtIsNull(Long userId);
    List<JobOffer> findByDeletedAtIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from JobOffer o where o.id = :id")
    Optional<JobOffer> findByIdForUpdate(@Param("id") Long id);
}

