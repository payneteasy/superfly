package com.payneteasy.superfly.security.csrf;

import com.payneteasy.superfly.security.exception.CsrfLoginTokenException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class CsrfValidatorImplTest {

    private static final String ATTRIBUTE_NAME = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    private HttpServletRequest request;
    private HttpSession session;
    private CsrfValidatorImpl validator;

    @Before
    public void setUp() {
        request = EasyMock.createMock(HttpServletRequest.class);
        session = EasyMock.createMock(HttpSession.class);
        validator = new CsrfValidatorImpl(true);
    }

    @Test
    public void persistTokenIntoSession_storesGeneratedTokenAndReturnsIt() {
        session.setAttribute(EasyMock.eq(ATTRIBUTE_NAME), EasyMock.anyObject(String.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            assertEquals(EasyMock.getCurrentArguments()[1].toString().length(), 36);
            return null;
        });
        EasyMock.replay(session);

        String token = validator.persistTokenIntoSession(session);

        assertNotNull(token);
        EasyMock.verify(session);
    }

    @Test
    public void persistTokenIntoSession_generatesDifferentTokens() {
        session.setAttribute(EasyMock.eq(ATTRIBUTE_NAME), EasyMock.anyObject());
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(session);

        assertNotEquals(validator.persistTokenIntoSession(session), validator.persistTokenIntoSession(session));
    }

    @Test
    public void validateToken_whenDisabled_doesNotTouchRequest() {
        EasyMock.replay(request);

        new CsrfValidatorImpl(false).validateToken(request);

        EasyMock.verify(request);
    }

    @Test(expected = IllegalStateException.class)
    public void validateToken_whenRequestIsNull_throwsIllegalState() {
        validator.validateToken(null);
    }

    @Test
    public void validateToken_whenNoSession_throws() {
        EasyMock.expect(request.getSession(false)).andReturn(null);
        EasyMock.replay(request);

        assertRejected("No session.");
    }

    @Test
    public void validateToken_whenNoTokenInSession_throws() {
        expectSessionToken(null);
        EasyMock.replay(request, session);

        assertRejected("No any CSRF token in the session. Please check server config.");
    }

    @Test
    public void validateToken_whenParameterMissing_throws() {
        expectSessionToken("abc");
        EasyMock.expect(request.getParameter("_csrf")).andReturn(null);
        EasyMock.replay(request, session);

        assertRejectedStartingWith("Missing CSRF token");
    }

    @Test
    public void validateToken_whenParameterEmpty_throws() {
        expectSessionToken("abc");
        EasyMock.expect(request.getParameter("_csrf")).andReturn("");
        EasyMock.replay(request, session);

        assertRejectedStartingWith("Missing CSRF token");
    }

    @Test
    public void validateToken_whenTokenMismatch_throws() {
        expectSessionToken("abc");
        EasyMock.expect(request.getParameter("_csrf")).andReturn("xyz");
        EasyMock.replay(request, session);

        assertRejected("Invalid CSRF token.");
    }

    @Test
    public void validateToken_whenTokenMatches_passes() {
        expectSessionToken("abc");
        EasyMock.expect(request.getParameter("_csrf")).andReturn("abc");
        EasyMock.replay(request, session);

        validator.validateToken(request);

        EasyMock.verify(request, session);
    }

    private void expectSessionToken(String token) {
        EasyMock.expect(request.getSession(false)).andReturn(session);
        EasyMock.expect(session.getAttribute(ATTRIBUTE_NAME)).andReturn(token);
    }

    private void assertRejected(String expectedMessage) {
        try {
            validator.validateToken(request);
            fail("CsrfLoginTokenException expected");
        } catch (CsrfLoginTokenException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    private void assertRejectedStartingWith(String prefix) {
        try {
            validator.validateToken(request);
            fail("CsrfLoginTokenException expected");
        } catch (CsrfLoginTokenException e) {
            org.junit.Assert.assertTrue(e.getMessage(), e.getMessage().startsWith(prefix));
        }
    }
}
