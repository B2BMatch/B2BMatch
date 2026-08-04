package com.b2bmatch.ofertas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.b2bmatch.ofertas.model.JobApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;
    private Long jobOfferId;
    private Long professionalId;
    private String proposal;
    private BigDecimal expectedPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobApplicationResponse fromEntity(JobApplication entity) {
        if (entity == null) return null;
        return new JobApplicationResponse(
            entity.getId(),
            entity.getJobOffer() != null ? entity.getJobOffer().getId() : null,
            entity.getProfessionalId(),
            entity.getProposal(),
            entity.getExpectedPrice(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
