package com.payneteasy.superfly.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

public class MultiStepLoginUrlAuthenticationEntryPointTest extends TestCase {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private MultiStepLoginUrlAuthenticationEntryPoint entryPoint;
	
	public void setUp() {
		request = EasyMock.createNiceMock(HttpServletRequest.class);
		response = EasyMock.createMock(HttpServletResponse.class);
		entryPoint = new MultiStepLoginUrlAuthenticationEntryPoint();
		entryPoint.setLoginFormUrl("/step-one.html");
		Map<Class<? extends Authentication>, String> mapping = new HashMap<Class<? extends Authentication>, String>();
		mapping.put(Step2Authentication.class, "/step-two.html");
		entryPoint.setInsufficientAuthenticationMapping(mapping);
		
		EasyMock.expect(request.getServerPort()).andReturn(80).anyTimes();
		EasyMock.expect(request.getScheme()).andReturn("http").anyTimes();
		EasyMock.expect(request.getServerName()).andReturn("localhost").anyTimes();
	}
	
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}
	
	public void testBadCredentials() throws Exception {
		initExpectingRedirect("http://localhost/step-one.html");
		
		EasyMock.replay(response, request);
		
		entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));
		
		EasyMock.verify(response);
	}

	private void initExpectingRedirect(String url) throws IOException {
		EasyMock.expect(response.encodeRedirectURL(url)).andReturn(url);
		response.sendRedirect(url);
		EasyMock.expectLastCall();
	}
	
	public void testStep2() throws Exception {
		initExpectingRedirect("http://localhost/step-two.html");
		
		EasyMock.replay(response, request);
		
		SecurityContextHolder.getContext().setAuthentication(new Step2Authentication());
		entryPoint.commence(request, response, new InsufficientAuthenticationException("Insufficient!"));
		
		EasyMock.verify(response);
	}
	
	public void testStep2AuthFromException() throws Exception {
		initExpectingRedirect("http://localhost/step-two.html");
		
		EasyMock.replay(response, request);
		
		InsufficientAuthenticationException auth = new InsufficientAuthenticationException("Insufficient!");
		auth.setAuthentication(new Step2Authentication());
		entryPoint.commence(request, response, auth);
		
		EasyMock.verify(response);
	}
	
	private static class Step2Authentication extends EmptyAuthenticationToken {
		private static final long serialVersionUID = 1L;
	}
}
