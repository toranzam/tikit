package com.tikit.payment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        String method,
        Long totalAmount,
        Long balanceAmount,
        String lastTransactionKey,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        List<Cancel> cancels
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancel(
            String transactionKey,
            Long cancelAmount,
            String cancelReason,
            OffsetDateTime canceledAt
    ) {
    }
}