package com.payneteasy.superfly.web.servlet;

import jakarta.servlet.http.Cookie;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertTrue;

public class CookieEnforcerTest {

    /** TLS is terminated in front of Jetty, so the request itself is plain http. */
    @Test
    public void cookiesAreSecureAndHttpOnlyOnPlainHttpRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CookieEnforcer().doFilter(request, response, (req, resp) ->
                ((jakarta.servlet.http.HttpServletResponse) resp).addCookie(new Cookie("any", "v")));

        Cookie cookie = response.getCookie("any");
        assertTrue("Secure", cookie.getSecure());
        assertTrue("HttpOnly", cookie.isHttpOnly());
    }
}
