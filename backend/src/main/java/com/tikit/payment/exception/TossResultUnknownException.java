package com.tikit.payment.exception;

// 요청을 보냈지만 응답을 받지 못해 결제 결과를 알 수 없는 경우
public class TossResultUnknownException extends RuntimeException {
    public TossResultUnknownException(String message) {
        super(message);
    }
}
