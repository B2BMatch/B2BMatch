package com.b2bmatch.ofertas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.b2bmatch.ofertas.model.JobOffer;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByCompanyId(Long companyId);
    List<JobOffer> findByStatusNot(String status);
}

