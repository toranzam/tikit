package com.tikit.seller.exception;

import com.tikit.common.ErrorCode;
import com.tikit.common.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.tikit.seller")
public class SellerExceptionHandler {

    @ExceptionHandler(SellerException.class)
    public ResponseEntity<ResponseDTO<Void>> handleSellerException(SellerException e){
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ResponseDTO.fail(errorCode));
    }

    // 판매자 신청 입력값 검증("요청 형식 또는 입력값을 확인해 주세요.")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Void>> handleValidation(MethodArgumentNotValidException e){

        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.fail(ErrorCode.VALIDATION_ERROR));
    }
}
