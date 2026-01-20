package com.babyvo.babyvo.common.apis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private boolean success;
    private int status;
    private String message;      // "OTP_INVALID", "OK" vs.
    private Instant timestamp;   // opsiyonel ama faydalı
    private String traceId;      // opsiyonel (log correlation)
    private T data;              // success payload
    private Map<String, Object> meta; // opsiyonel (paging vs.)
    private ApiError error;      // error payload (sadece success=false)

    public static <T> ApiResult<T> ok(T data) {
        return ApiResult.<T>builder()
                .success(true)
                .status(200)
                .message("OK")
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    public static <T> ApiResult<T> ok(String message, T data) {
        return ApiResult.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    public static <T> ApiResult<T> fail(int status, String message, ApiError error) {
        return ApiResult.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .timestamp(Instant.now())
                .error(error)
                .build();
    }

    public static <T>ApiResult<T> ok() {
        return ApiResult.<T>builder()
                .success(false)
                .status(200)
                .message("SUCCESS")
                .timestamp(Instant.now())
                .build();

    }
}