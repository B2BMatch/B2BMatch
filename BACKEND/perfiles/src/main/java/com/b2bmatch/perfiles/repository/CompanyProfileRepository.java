package com.b2bmatch.perfiles.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.b2bmatch.perfiles.model.CompanyProfile;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {

    Optional<CompanyProfile> findByUserId(Long userId);

    Optional<CompanyProfile> findByTaxId(String taxId);

    List<CompanyProfile> findByStatusNot(String status);

}
