package com.payneteasy.superfly.web.security.securehandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

public class SecuredHttpRequestHandler implements HttpRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SecuredHttpRequestHandler.class);

    private final HttpRequestHandler   delegate;
    private final IAuthorizationParser parser;

    public SecuredHttpRequestHandler(HttpRequestHandler aDelegate, IAuthorizationParser aParser) {
        delegate = aDelegate;
        parser = aParser;
    }

    @Override
    public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {

        String id = aRequest.getRemoteAddr() + "#" + aRequest.getPathInfo();

        String authorization = aRequest.getHeader("Authorization");
        if(authorization == null) {
            LOG.warn("{}: No Authorization header", id);
            aResponse.sendError(SC_UNAUTHORIZED, "No 'Authorization' header");
            return;
        }

        try {
            AuthorizationBearer bearer = parser.parse(authorization);
            checkCredentials(bearer);
            LOG.debug("{}: OK - {} - {}", id, authorization, bearer);

            getContext().setAuthentication(new UsernamePasswordAuthenticationToken(bearer.subsystem, null));
            try {
                delegate.handleRequest(aRequest, aResponse);
            } finally {
                getContext().setAuthentication(null);
            }

        } catch (AuthorizationException e) {
            LOG.warn("{}: {}", id, e.getMessage());
            aResponse.sendError(SC_FORBIDDEN, e.getMessage());
        }

    }

    private void checkCredentials(AuthorizationBearer aBearer) throws AuthorizationException {

        String expectedToken = System.getenv(aBearer.subsystem);
        if(expectedToken == null) {
            expectedToken = System.getProperty(aBearer.subsystem);
        }

        if(expectedToken == null) {
            throw new AuthorizationException("There are no token for {}", aBearer.subsystem);
        }

        if(!expectedToken.equals(aBearer.token)) {
            throw new AuthorizationException("Wrong access token for {}", aBearer.subsystem);
        }

    }

}
