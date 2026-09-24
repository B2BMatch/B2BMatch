package com.b2bmatch.ofertas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferStatusRequest {

    @NotBlank(message = "status es obligatorio")
    private String status;
}