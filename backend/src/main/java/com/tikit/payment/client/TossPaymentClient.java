package com.tikit.payment.client;

import com.tikit.payment.client.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final RestClient tossRestClient;

    public TossPaymentResponse getPayment(String paymentKey) {
        return tossRestClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .body(TossPaymentResponse.class);
    }
}
