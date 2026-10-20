package com.tikit.common.advice;

import com.tikit.common.ErrorCode;
import com.tikit.common.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDTO<Void>> handleInvalidJson(
            HttpMessageNotReadableException e
    ) {

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(
                        errorCode,
                        "JSON 파싱에 실패하였습니다."));
    }
}
