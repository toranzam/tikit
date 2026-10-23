package com.tikit.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResponseDTO(
        String code,
        String message
) {
}