package com.educational.student_risk_assessment.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            EntityNotFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", message, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String message = String.format("Parameter '%s' must be of type '%s'",
                ex.getName(), ex.getRequiredType().getSimpleName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Type Mismatch", message, req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArg(
            IllegalArgumentException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    // Catch-all handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(
            Exception ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(
            DataAccessException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Data Access Error",
                "Unable to access student records", req.getRequestURI());
    }



    // Helper
    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String error, String message, String path) {
        ApiErrorResponse resp = new ApiErrorResponse(status.value(), error, message, path);
        return new ResponseEntity<>(resp, status);
    }
}
