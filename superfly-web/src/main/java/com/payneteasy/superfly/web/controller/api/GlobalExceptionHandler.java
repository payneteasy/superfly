package com.payneteasy.superfly.web.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.DisconnectedClientHelper;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @return the answer for the client; {@code null} (nothing is written) when the client has closed the connection,
     * for example a long poll dropped while a subsystem restarts: there is nobody to answer and nothing to fix on the
     * server, so it is one DEBUG line without a stack trace, not an error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        if (e instanceof IOException && DisconnectedClientHelper.isClientDisconnectedException(e)) {
            log.debug("The client closed the connection: {}", e.toString());
            return null;
        }
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled error errorId={}", errorId, e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error", "errorId", errorId));
    }
}
