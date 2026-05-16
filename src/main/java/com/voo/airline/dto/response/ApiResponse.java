package com.voo.airline.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean       success,
    String        message,
    T             data,
    String        error,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, null, null, error, LocalDateTime.now());
    }
}
