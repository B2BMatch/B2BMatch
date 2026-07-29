package com.b2bmatch.ofertas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.b2bmatch.ofertas.model.Quotation;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findByServiceId(Long serviceId);
    List<Quotation> findByCustomerId(Long customerId);
}

