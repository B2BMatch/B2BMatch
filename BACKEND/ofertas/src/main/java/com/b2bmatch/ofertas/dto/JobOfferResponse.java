package com.b2bmatch.ofertas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.b2bmatch.ofertas.model.JobOffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferResponse {
    private Long id;
    private Long companyId;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal budget;
    private LocalDate deadline;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobOfferResponse fromEntity(JobOffer entity) {
        if (entity == null) return null;
        return new JobOfferResponse(
            entity.getId(),
            entity.getCompanyId(),
            entity.getUserId(),
            entity.getCategoryId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getBudget(),
            entity.getDeadline(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
