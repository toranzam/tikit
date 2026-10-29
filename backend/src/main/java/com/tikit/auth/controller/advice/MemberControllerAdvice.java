package com.tikit.auth.controller.advice;

import com.tikit.auth.exception.LoginException;
import com.tikit.auth.cookie.RefreshTokenCookie;
import com.tikit.auth.exception.RefreshTokenException;
import com.tikit.common.ErrorCode;
import com.tikit.common.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class MemberControllerAdvice {

    private final RefreshTokenCookie refreshTokenCookie;

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ResponseDTO<Void>> handleLoginException(LoginException e) {
        log.error("login failed: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(
                        errorCode
                ));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ResponseDTO<Void>> handleRefreshException(RefreshTokenException e) {
        log.error("refresh failed: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.REFRESH_TOKEN_INVALID;

        return ResponseEntity
                .status(errorCode.getStatus())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString())
                .body(ResponseDTO.fail(
                        errorCode
                ));
    }

}
