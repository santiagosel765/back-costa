package com.ferrisys.config.web;

import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.NotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatValidationError)
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "Validation failed";
        }

        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request, message);
    }

    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            RuntimeException exception, HttpServletRequest request) {
        String message = exception.getMessage() != null ? exception.getMessage() : "Bad request";
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request, message);
    }

    @ExceptionHandler({EntityNotFoundException.class, NotFoundException.class})
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            RuntimeException exception, HttpServletRequest request) {
        String message = exception.getMessage() != null ? exception.getMessage() : "Resource not found";
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN, request, "Access is denied");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(
            ExpiredJwtException exception, HttpServletRequest request) {
        return buildErrorResponse(exception, HttpStatus.UNAUTHORIZED, request, "Token expired");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception, HttpServletRequest request) {
        String message = "Unexpected error. Please contact support if the problem persists.";
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, request, message);
    }

    private String formatValidationError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception exception, HttpStatus status, HttpServletRequest request, String message) {
        if (status.is5xxServerError()) {
            log.error(
                    "{} {} resulted in {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status.value(),
                    status.getReasonPhrase(),
                    exception);
        } else {
            log.warn(
                    "{} {} resulted in {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status.value(),
                    status.getReasonPhrase(),
                    message);
        }

        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }

    private record ErrorResponse(
            OffsetDateTime timestamp, int status, String error, String message, String path) {}
}
