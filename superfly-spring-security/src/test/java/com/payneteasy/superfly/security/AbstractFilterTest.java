package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.junit.Before;

public abstract class AbstractFilterTest extends AbstractSSOUserAwareTest {
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected FilterChain chain;
	protected HttpSession session;
	protected Filter filter;

    @Before
	public void initFilterTest() {
		request = EasyMock.createMock(HttpServletRequest.class);
		expect(request.getContextPath()).andReturn("").anyTimes();
		expect(request.getMethod()).andReturn("POST").anyTimes();
		response = EasyMock.createMock(HttpServletResponse.class);
		chain = EasyMock.createMock(FilterChain.class);
		session = EasyMock.createMock(HttpSession.class);
	}

	protected void expectRedirectTo(String url) throws IOException {
		expect(response.encodeRedirectURL(url)).andReturn(url);
		response.sendRedirect(url);
		expectLastCall();
	}

}