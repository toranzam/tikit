package com.tikit.seller.exception;

import com.tikit.common.ErrorCode;
import lombok.Getter;

@Getter
public class SellerException extends RuntimeException{

    private final ErrorCode errorCode;

    public SellerException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
