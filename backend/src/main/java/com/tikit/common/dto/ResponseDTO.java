package com.tikit.common.dto;

import com.tikit.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class ResponseDTO<T> {
    private final String code;
    private final String message;
    private final T data;

    public static <T> ResponseDTO<T> success(
            String message,
            T data
    ) {
        return new ResponseDTO<>("SUCCESS", message, data);
    }

    public static <T> ResponseDTO<T> success(
            String message
    ) {
        return new ResponseDTO<>("SUCCESS", message, null);
    }

    public static ResponseDTO<Void> fail(ErrorCode errorCode) {
        return new ResponseDTO<>(
                errorCode.name(),
                errorCode.getMessage(),
                null
        );
    }

    public static ResponseDTO<Void> fail(
            ErrorCode errorCode,
            String message
    ) {
        return new ResponseDTO<>(errorCode.name(),
                message,
                null
        );
    }
}
