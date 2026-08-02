package com.b2bmatch.ofertas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequest {

    @NotNull(message = "serviceId is required")
    private Long serviceId;

    @NotNull(message = "customerId is required")
    private Long customerId;

    private String message;

    private String status;
}
