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
 * Global exception handler for API errors. This class handles exceptions
 * thrown during request processing and returns appropriate HTTP responses with error messages.
 */
@RestControllerAdvice
public class UserRouteApiExceptionHandler {

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

    @ExceptionHandler(MissingInformationException.class)
    public ResponseEntity<Map<String, Object>> handleMissingInformation(MissingInformationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "Missing information",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "error", "Unauthorized",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "error", "User not found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(UnauthenticatedDeletionRequest.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDeletionRequest(UnauthenticatedDeletionRequest ex) {
        System.out.println("Unauthenticated deletion request: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", "You cannot perform this action"
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "error", "User not found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "error", "Unauthorized",
                        "message", "Username or password is incorrect"
                )
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "error", "Unauthorized",
                        "message", "Invalid or expired refresh token"
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        System.out.println("Unhandled exception: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "error", "Internal server error",
                        "message", "An unexpected error occurred"
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleUnsupportedHttpRequest(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                Map.of(
                        "error", "Method not allowed",
                        "message", "The requested method is not allowed for this endpoint"
                )
        );
    }
}