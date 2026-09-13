package com.fxlso.handlers;

import com.fxlso.exceptions.MissingInformationException;
import com.fxlso.exceptions.UserAlreadyExistsException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for API errors. This class handles exceptions
 * thrown during request processing and returns appropriate HTTP responses with error messages.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    public record RegisterRequest(@NotBlank String username, String password) {}

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "Invalid request body",
                        "message", "Body must be valid JSON with required fields"
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "Validation failed",
                        "message", "One or more fields are invalid"
                )
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "error", "User already exists",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(com.fxlso.exceptions.MissingInformationException.class)
    public ResponseEntity<Map<String, Object>> handleMissingInformation(MissingInformationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "Missing information",
                        "message", ex.getMessage()
                )
        );
    }

}