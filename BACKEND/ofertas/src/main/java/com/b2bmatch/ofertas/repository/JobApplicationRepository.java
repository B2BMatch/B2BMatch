package com.b2bmatch.ofertas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.b2bmatch.ofertas.model.JobApplication;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobOfferId(Long jobOfferId);
    List<JobApplication> findByProfessionalId(Long professionalId);
}

