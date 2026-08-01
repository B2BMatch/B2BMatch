package com.b2bmatch.perfiles.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.b2bmatch.perfiles.model.ProfessionalProfile;

@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, Long> {

    Optional<ProfessionalProfile> findByUserId(Long userId);
    List<ProfessionalProfile> findByStatusNot(String status);

}
