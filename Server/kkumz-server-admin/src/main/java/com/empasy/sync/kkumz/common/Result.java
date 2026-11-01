package com.empasy.sync.kkumz.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kkumz 전사 표준 API 응답 래퍼
 * Vben Admin (code, success, message, result) 및 Web 포털 (data) 동시 지원
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private boolean success;
    private String message;
    private T result;
    private T data;
    private long timestamp;

    public static <T> Result<T> OK(T payload) {
        return Result.<T>builder()
                .code(200)
                .success(true)
                .message("OK")
                .result(payload)
                .data(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> OK(String message, T payload) {
        return Result.<T>builder()
                .code(200)
                .success(true)
                .message(message)
                .result(payload)
                .data(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(500)
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
