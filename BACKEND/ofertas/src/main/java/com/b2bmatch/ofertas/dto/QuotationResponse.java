package com.b2bmatch.ofertas.dto;

import java.time.LocalDateTime;
import com.b2bmatch.ofertas.model.Quotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationResponse {
    private Long id;
    private Long serviceId;
    private Long customerId;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuotationResponse fromEntity(Quotation entity) {
        if (entity == null) return null;
        return new QuotationResponse(
            entity.getId(),
            entity.getServiceId(),
            entity.getCustomerId(),
            entity.getMessage(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
