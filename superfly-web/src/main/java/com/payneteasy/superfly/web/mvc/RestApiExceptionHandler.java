package com.payneteasy.superfly.web.mvc;

import com.payneteasy.superfly.web.mvc.model.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.UUID;

/**
 * Lives in the rest-api child context only (web.mvc is excluded from the root scan), so it does not
 * affect other servlets. A controller-local handler cannot catch "no handler" (there is no controller yet),
 * and the root GlobalExceptionHandler would turn it into 500.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestApiExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", "Not found", "Unknown endpoint");
        error.setErrorId(UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("Content-Language", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}
