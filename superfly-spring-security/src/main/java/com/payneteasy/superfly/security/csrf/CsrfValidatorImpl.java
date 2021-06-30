package com.payneteasy.superfly.security.csrf;

import com.payneteasy.superfly.security.exception.CsrfLoginTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * CSRF token must be contained in parameter '_csrf'
 *
 * @author Igor Vasilev
 */
public class CsrfValidatorImpl implements CsrfValidator {
    private static final Logger logger = LoggerFactory.getLogger(CsrfValidatorImpl.class);
    private static final String ATTRIBUTE_NAME = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    private boolean disabled;

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String persistTokenIntoSession(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute(ATTRIBUTE_NAME, token);
        return token;
    }

    @Override
    public void validateToken(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalStateException("Cannot get request attribute - request is null!");
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.warn("No session");
            throw new CsrfLoginTokenException("No session.");
        }

        String token = (String) session.getAttribute(ATTRIBUTE_NAME);

        if (disabled) {
            logger.warn("CSRF login token check is disabled");
        }

        if (token == null) {
            logger.error("No {} value in session", ATTRIBUTE_NAME);
            throw new CsrfLoginTokenException("No any CSRF token in the session. Please check server config.");
        }

        String csrf = request.getParameter("_csrf");
        if (csrf == null || csrf.isEmpty()) {
            logger.error("Field _csrf is empty in request");
            throw new CsrfLoginTokenException("Missing CSRF token. This can be caused by ad- or script-blocking plugins, but also by the browser itself if it's not allowed to set cookies.");
        }

        if (!csrf.equals(token)) {
            logger.error("CSRF is invalid. Expected {} but was {}", token, csrf);
            throw new CsrfLoginTokenException("Invalid CSRF token.");
        }
    }
}
