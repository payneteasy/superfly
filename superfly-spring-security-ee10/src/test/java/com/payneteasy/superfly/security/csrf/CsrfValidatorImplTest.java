package com.payneteasy.superfly.security.csrf;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.security.exception.CsrfLoginTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.easymock.EasyMock;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class CsrfValidatorImplTest {

    private static final String ATTRIBUTE_NAME = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    @Test
    public void validateToken_whenTokenMismatch_throwsAndDoesNotLogTokens() {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        EasyMock.expect(request.getSession(false)).andReturn(session);
        EasyMock.expect(session.getAttribute(ATTRIBUTE_NAME)).andReturn("session-secret-token");
        EasyMock.expect(request.getParameter("_csrf")).andReturn("request-secret-token");
        EasyMock.replay(request, session);

        Logger logger = (Logger) LoggerFactory.getLogger(CsrfValidatorImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            new CsrfValidatorImpl(true).validateToken(request);
            fail("CsrfLoginTokenException expected");
        } catch (CsrfLoginTokenException e) {
            assertEquals("Invalid CSRF token.", e.getMessage());
        } finally {
            logger.detachAppender(appender);
        }

        assertFalse(appender.list.isEmpty());
        for (ILoggingEvent event : appender.list) {
            String text = event.getFormattedMessage() + " " + Arrays.toString(event.getArgumentArray());
            assertFalse(text, text.contains("session-secret-token"));
            assertFalse(text, text.contains("request-secret-token"));
        }
    }
}
