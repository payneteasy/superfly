package com.payneteasy.superfly.security.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface CsrfValidator {
    String persistTokenIntoSession(HttpSession session);

    void validateToken(HttpServletRequest request);
}
