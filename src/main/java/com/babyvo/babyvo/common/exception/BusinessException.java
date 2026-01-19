package com.babyvo.babyvo.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String messageKey; // OTP_INVALID vb.
    private final String detail;     // opsiyonel

    public BusinessException(HttpStatus status, String messageKey) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
        this.detail = null;
    }

    public BusinessException(HttpStatus status, String messageKey, String detail) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
        this.detail = detail;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessageKey() { return messageKey; }
    public String getDetail() { return detail; }

    public static BusinessException badRequest(String key) {
        return new BusinessException(HttpStatus.BAD_REQUEST, key);
    }

    public static BusinessException conflict(String key) {
        return new BusinessException(HttpStatus.CONFLICT, key);
    }

    public static BusinessException unauthorized(String key) {
        return new BusinessException(HttpStatus.UNAUTHORIZED, key);
    }
}