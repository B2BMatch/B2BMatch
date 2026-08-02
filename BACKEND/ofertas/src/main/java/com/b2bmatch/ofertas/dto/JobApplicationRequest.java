package com.b2bmatch.ofertas.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationRequest {

    @NotNull(message = "jobOfferId is required")
    private Long jobOfferId;

    @NotNull(message = "professionalId is required")
    private Long professionalId;

    private Long userId;

    @NotBlank(message = "proposal is required")
    private String proposal;

    private BigDecimal expectedPrice;

    private String status;
}
