package com.ferrisys.config.web;

import com.ferrisys.common.api.ApiError;
import com.ferrisys.common.api.ErrorCode;
import com.ferrisys.common.exception.ModuleNotLicensedException;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ModuleNotLicensedException.class)
    public ResponseEntity<ApiError> handleModuleNotLicensed(ModuleNotLicensedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(ErrorCode.MODULE_NOT_LICENSED, "Module not licensed", HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler({EntityNotFoundException.class, NotFoundException.class})
    public ResponseEntity<ApiError> handleEntityNotFound(RuntimeException exception) {
        String message = exception.getMessage() != null ? exception.getMessage() : "Entity not found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(ErrorCode.ENTITY_NOT_FOUND, message, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class, BadRequestException.class})
    public ResponseEntity<ApiError> handleValidation(Exception exception) {
        String message = exception.getMessage() != null ? exception.getMessage() : "Validation error";
        return ResponseEntity.badRequest()
                .body(ApiError.of(ErrorCode.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiError> handleUnauthorized(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(ErrorCode.UNAUTHORIZED, "Unauthorized", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(ErrorCode.UNAUTHORIZED, "Unauthorized", HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(ErrorCode.INTERNAL_ERROR, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
