package com.tikit.payment.exception;

// Toss를 사용할 수 없는 경우 (Toss 장애, 키 설정 문제, 연결 실패)
public class TossUnavailableException extends RuntimeException {
    public TossUnavailableException(String message) {
        super(message);
    }
}
