package com.tikit.payment.controller;

import com.tikit.common.dto.ResponseDTO;
import com.tikit.payment.dto.response.PaymentAttemptResponseDTO;
import com.tikit.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payment-attempts")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 처리 결과 조회
    @GetMapping("/{paymentAttemptId}")
    public ResponseEntity<ResponseDTO<PaymentAttemptResponseDTO>> getPaymentAttempt (
            @PathVariable Long paymentAttemptId,
            @AuthenticationPrincipal String accountId
    ) {
        PaymentAttemptResponseDTO response =
                paymentService.getPaymentAttempt(paymentAttemptId, Long.valueOf(accountId));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDTO.success("결제 처리 결과 조회 성공", response));
    }
}
