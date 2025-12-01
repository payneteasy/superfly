package com.payneteasy.superfly.web.mvc;

import com.payneteasy.superfly.service.RemoteAuthService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.web.mvc.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
@RestController
public class RemoteAuthCheckController {

    private final RemoteAuthService remoteAuthService;

    @Autowired
    public RemoteAuthCheckController(RemoteAuthService remoteAuthService) {
        this.remoteAuthService = remoteAuthService;
    }

    @RequestMapping(value = "/check-password/{subsystemName}/{username}", method = RequestMethod.POST, produces = "application/json")
    public CheckPasswordResponse checkPassword(@PathVariable String subsystemName,
                                               @PathVariable String username,
                                               @RequestBody CheckPasswordRequest requestBody,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader,
                                               HttpServletRequest request) throws RemoteAuthException {

        String bearerToken = extractBearerToken(authHeader);
        if (bearerToken == null) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        if (requestBody.getPasswordEncrypted() == null) {
            throw new BadRequestException("Missing passwordEncrypted field");
        }

        if (!username.equals(requestBody.getUsername())) {
            throw new BadRequestException("Username in path and body must match");
        }

        String sessionToken = remoteAuthService.checkPassword(subsystemName, username, requestBody.getPasswordEncrypted(), bearerToken, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new CheckPasswordResponse(username, sessionToken);
    }

    @RequestMapping(value = "/check-otp/{subsystemName}/{username}", method = RequestMethod.POST, produces = "application/json")
    public CheckOtpResponse checkOtp(@PathVariable String subsystemName,
                                     @PathVariable String username,
                                     @RequestBody CheckOtpRequest requestBody,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) throws RemoteAuthException {

        String bearerToken = extractBearerToken(authHeader);
        if (bearerToken == null) {
             throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        if (requestBody.getOtpEncrypted() == null || requestBody.getSessionToken() == null) {
             throw new BadRequestException("Missing otpEncrypted or sessionToken field");
        }

        if (!username.equals(requestBody.getUsername())) {
            throw new BadRequestException("Username in path and body must match");
        }

        String result = remoteAuthService.checkOtp(subsystemName, username, requestBody.getOtpEncrypted(), requestBody.getSessionToken(), bearerToken);
        return new CheckOtpResponse(username, requestBody.getSessionToken(), result);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @ExceptionHandler(RemoteAuthException.class)
    public ResponseEntity<ErrorResponse> handleRemoteAuthException(RemoteAuthException e) {
        String errorCode = e.getErrorCode();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if ("INTERNAL_ERROR".equals(errorCode)) {
             status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return createErrorResponse(errorCode, e.getMessage(), status);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return createErrorResponse("UNAUTHORIZED", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        return createErrorResponse("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonError(HttpMessageNotReadableException e) {
        return createErrorResponse("BAD_REQUEST", "Invalid JSON", HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(String type, String title, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(type, title, title);
        error.setErrorId(UUID.randomUUID().toString());

        return ResponseEntity.status(status)
                .header("Content-Language", "en")
                .body(error);
    }

    static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }

    static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) { super(message); }
    }
}
