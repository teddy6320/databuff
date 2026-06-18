package com.databuff.apm.web.ai.platform;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = {
        "com.databuff.apm.web.ai.platform.api",
        "com.databuff.apm.web.ai"
})
public class AiPlatformExceptionHandler {

    @ExceptionHandler(AiPlatformApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(AiPlatformApiException exception) {
        return ResponseEntity.status(HttpStatus.valueOf(exception.httpStatus()))
                .body(Map.of(
                        "error", exception.errorCode(),
                        "message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "bad_request",
                        "message", exception.getMessage() == null ? "invalid request" : exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "conflict",
                        "message", exception.getMessage() == null ? "conflict" : exception.getMessage()));
    }
}
