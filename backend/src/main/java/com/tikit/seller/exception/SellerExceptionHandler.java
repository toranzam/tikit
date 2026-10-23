package com.tikit.seller.exception;

import com.tikit.common.ErrorCode;
import com.tikit.common.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
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
}
