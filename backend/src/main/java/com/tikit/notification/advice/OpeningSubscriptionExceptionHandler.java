package com.tikit.notification.advice;

import com.tikit.common.ErrorCode;
import com.tikit.common.dto.ResponseDTO;
import com.tikit.notification.controller.OpeningSubscriptionController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;


// TODO: 이름 짱 길다
// 예외 처리 - 공연별 알림 API
@RestControllerAdvice(assignableTypes = OpeningSubscriptionController.class)
public class OpeningSubscriptionExceptionHandler {

    // 중복 신청 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDTO<Void>> handleDuplicate(
            IllegalStateException e
    ) {
        ErrorCode errorCode = ErrorCode.STATE_CONFLICT;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(errorCode, e.getMessage()));
    }

    // 알림 신청 내역 없음 처리
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseDTO<Void>> handleNotFound(
            NoSuchElementException e
    ) {
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(errorCode, e.getMessage()));
    }

    // 인증 정보 없음 처리
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ResponseDTO<Void>> handleUnauthorized(
            AuthenticationCredentialsNotFoundException e
    ) {
        ErrorCode errorCode = ErrorCode.AUTHENTICATION_REQUIRED;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(errorCode, e.getMessage()));
    }
}
