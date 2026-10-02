package com.fxlso.handlers;

import com.fxlso.exceptions.*;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * API Key Related Exception Handler
 */
@RestControllerAdvice
public class ApiKeyExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleInvalidApiKeyException(InvalidApiKeyException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Invalid API Key",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleNoActiveKeysException(NoActiveKeysException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "No Active API Keys",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleApiGenerationException(ApiGenerationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "API Key Generation Error",
                "message", ex.getMessage()
        ));
    }
}