package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import org.easymock.EasyMock;
import org.junit.Before;

public abstract class AbstractFilterTest extends AbstractSSOUserAwareTest {
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected FilterChain chain;
    protected HttpSession session;
    protected Filter filter;
    protected CsrfValidator csrfValidator;

    @Before
    public void initFilterTest() {
        request = EasyMock.createMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("").anyTimes();
        expect(request.getMethod()).andReturn("POST").anyTimes();
        response = EasyMock.createMock(HttpServletResponse.class);
        expect(response.isCommitted()).andReturn(false).anyTimes();
        chain = EasyMock.createMock(FilterChain.class);
        session = EasyMock.createMock(HttpSession.class);
        csrfValidator = EasyMock.createMock(CsrfValidator.class);
    }

    protected void expectRedirectTo(String url) throws IOException {
        expect(response.encodeRedirectURL(url)).andReturn(url);
        response.sendRedirect(url);
        expectLastCall();
    }

}
