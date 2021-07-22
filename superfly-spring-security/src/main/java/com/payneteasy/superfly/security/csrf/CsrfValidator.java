package com.payneteasy.superfly.security.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface CsrfValidator {
    String persistTokenIntoSession(HttpSession session);

    void validateToken(HttpServletRequest request);
}
