package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.client.SSOLoginState;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.easymock.Capture;
import org.junit.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SuperflySSOLoginUrlAuthenticationEntryPointTest {

    private static final String LOGIN_URL = "https://sso.example/sso/login?subsystemIdentifier=sys&targetUrl=%2Fapp%2Fpage";

    @Test
    public void testRedirectCarriesStateKeptInSession() throws Exception {
        String[] first = commence();
        assertTrue(first[0], first[0].matches("^[A-Za-z0-9_-]{43}$"));
        assertEquals(LOGIN_URL + "&state=" + first[0], first[1]);

        String[] second = commence();
        assertNotEquals("every login gets a new state", first[0], second[0]);
    }

    /** @return stored state and redirect location */
    private static String[] commence() throws Exception {
        SuperflySSOLoginUrlAuthenticationEntryPoint entryPoint = new SuperflySSOLoginUrlAuthenticationEntryPoint(LOGIN_URL);
        entryPoint.afterPropertiesSet();

        HttpSession session = createMock(HttpSession.class);
        Capture<Object> stored = Capture.newInstance();
        session.setAttribute(eq(SSOLoginState.SESSION_ATTRIBUTE), capture(stored));
        expectLastCall().once();
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getSession(true)).andReturn(session).anyTimes();
        expect(request.getSession()).andReturn(session).anyTimes();
        expect(request.getScheme()).andReturn("https").anyTimes();
        expect(request.getServerName()).andReturn("app.example").anyTimes();
        expect(request.getServerPort()).andReturn(443).anyTimes();
        HttpServletResponse response = createMock(HttpServletResponse.class);
        Capture<String> encoded = Capture.newInstance();
        expect(response.encodeRedirectURL(capture(encoded))).andAnswer(encoded::getValue).anyTimes();
        Capture<String> location = Capture.newInstance();
        response.sendRedirect(capture(location));
        expectLastCall().once();
        replay(session, request, response);

        entryPoint.commence(request, response, new InsufficientAuthenticationException("login required"));

        verify(session, response);
        return new String[]{(String) stored.getValue(), location.getValue()};
    }
}
