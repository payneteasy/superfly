package com.payneteasy.superfly.web.security.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.payneteasy.superfly.common.utils.LogSanitizer.forLog;

@Slf4j
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final Gson gson = new Gson();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        // Exception messages can name the subsystem and tell "not found" from "no token" — keep them in
        // the server log only, callers get the same text for every failure.
        String reason = forLog(exception.getMessage());
        log.warn("Authentication failed for {}: {}", request.getRequestURI(), reason);
        error.put("message", "Authentication failed");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.getWriter().write(gson.toJson(error));
    }
}
