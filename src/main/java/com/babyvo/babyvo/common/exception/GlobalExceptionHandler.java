package com.babyvo.babyvo.common.exception;

import com.babyvo.babyvo.common.apis.ApiError;
import com.babyvo.babyvo.common.apis.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException e, HttpServletRequest req) {
        ApiError err = ApiError.builder()
                .detail(e.getDetail())
                .build();

        ApiResult<Void> body = ApiResult.fail(e.getStatus().value(), e.getMessageKey(), err);
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<ApiError.FieldErrorItem> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorItem)
                .toList();

        ApiError err = ApiError.builder()
                .fieldErrors(fieldErrors)
                .build();

        ApiResult<Void> body = ApiResult.fail(400, "VALIDATION_ERROR", err);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGeneric(Exception e) {
        ApiError err = ApiError.builder()
                .detail("UNEXPECTED_ERROR")
                .build();

        ApiResult<Void> body = ApiResult.fail(500, "INTERNAL_SERVER_ERROR", err);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiError.FieldErrorItem toFieldErrorItem(FieldError fe) {
        return ApiError.FieldErrorItem.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .build();
    }
}