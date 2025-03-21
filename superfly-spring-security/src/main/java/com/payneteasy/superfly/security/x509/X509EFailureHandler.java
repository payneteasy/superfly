package com.payneteasy.superfly.security.x509;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.io.PrintWriter;

public class X509EFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeTextMessage(response, exception);
    }

    private void writeTextMessage(HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setHeader("Content-Type", "text/plain");

        PrintWriter writer = response.getWriter();
        writer.print(exception.getMessage());
        writer.close();
    }
}
