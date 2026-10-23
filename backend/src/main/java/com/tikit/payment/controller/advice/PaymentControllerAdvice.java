package com.tikit.payment.controller.advice;

import com.tikit.common.ErrorCode;
import com.tikit.common.dto.ResponseDTO;
import com.tikit.payment.exception.TossApiException;
import com.tikit.payment.exception.TossResultUnknownException;
import com.tikit.payment.exception.TossUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class PaymentControllerAdvice {

    @ExceptionHandler(TossApiException.class)
    public ResponseEntity<ResponseDTO<Void>> handleTossApiException(TossApiException e) {
        log.error("toss api declined: status={}, code={}, message={}", e.getHttpStatus(), e.getTossCode(), e.getMessage());

        ErrorCode errorCode = ErrorCode.PAYMENT_DECLINED;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(
                        errorCode
                ));
    }

    @ExceptionHandler(TossUnavailableException.class)
    public ResponseEntity<ResponseDTO<Void>> handleTossUnavailableException(TossUnavailableException e) {
        log.error("toss unavailable: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.SERVICE_UNAVAILABLE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(
                        errorCode
                ));
    }

    @ExceptionHandler(TossResultUnknownException.class)
    public ResponseEntity<ResponseDTO<Void>> handleTossResultUnknownException(TossResultUnknownException e) {
        log.error("toss result unknown: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.PAYMENT_IN_PROGRESS;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(
                        errorCode
                ));
    }
}
