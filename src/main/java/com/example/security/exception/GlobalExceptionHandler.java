package com.example.security.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import com.example.security.model.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleBadCredentials(
            RuntimeException exception,
            ServletWebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng", request);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiError> handleAccountStatus(
            AccountStatusException exception,
            ServletWebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Tài khoản hiện không thể đăng nhập", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleConflict(
            IllegalArgumentException exception,
            ServletWebRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            ServletWebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "Dữ liệu gửi lên không hợp lệ",
                request.getRequest().getRequestURI(),
                fields);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String message,
            ServletWebRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequest().getRequestURI()));
    }
}
