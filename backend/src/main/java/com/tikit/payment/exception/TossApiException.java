package com.tikit.payment.exception;

import lombok.Getter;

// Toss가 요청을 거절한 경우 (4xx)
@Getter
public class TossApiException extends RuntimeException {
    private final int httpStatus;
    private final String tossCode;

    public TossApiException(int httpStatus, String tossCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.tossCode = tossCode;
    }
}
