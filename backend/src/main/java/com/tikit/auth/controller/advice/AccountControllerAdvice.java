package com.tikit.auth.controller.advice;

import com.tikit.auth.exception.LoginException;
import com.tikit.common.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AccountControllerAdvice {

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ResponseDTO<Void>> handleLoginException(LoginException e) {
        log.error("login failed: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(-1, "login failed check your credentials", null));
    }
}
