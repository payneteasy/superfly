package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertSame;

public class MultiStepLoginUrlAuthenticationEntryPointTest {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private MultiStepLoginUrlAuthenticationEntryPoint entryPoint;

    @Before
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

    @After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

    @Test
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

    @Test
	public void testStep1() throws Exception {
		initExpectingRedirect("http://localhost/step-one.html");
		
		EasyMock.replay(response, request);
		
		Step1Authentication auth = new Step1Authentication();
		InsufficientAuthenticationException ex = new InsufficientAuthenticationException("Insufficient!");
		ex.setAuthentication(auth);
		entryPoint.commence(request, response, ex);
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
		
		EasyMock.verify(response);
	}

    @Test
	public void testStep2() throws Exception {
		initExpectingRedirect("http://localhost/step-two.html");
		
		EasyMock.replay(response, request);
		
		Step2Authentication auth = new Step2Authentication();
		SecurityContextHolder.getContext().setAuthentication(auth);
		entryPoint.commence(request, response, new InsufficientAuthenticationException("Insufficient!"));
		
		EasyMock.verify(response);
	}

    @Test
	public void testStep2AuthFromException() throws Exception {
		initExpectingRedirect("http://localhost/step-two.html");
		
		EasyMock.replay(response, request);
		
		InsufficientAuthenticationException ex = new InsufficientAuthenticationException("Insufficient!");
		Step2Authentication auth = new Step2Authentication();
		ex.setAuthentication(auth);
		entryPoint.commence(request, response, ex);
        assertSame(auth, SecurityContextHolder.getContext().getAuthentication());
		
		EasyMock.verify(response);
	}

    @Test
	public void testStep2FromCompound() throws Exception {
		initExpectingRedirect("http://localhost/step-two.html");
		
		EasyMock.replay(response, request);
		
		Step2Authentication auth = new Step2Authentication();
		CompoundAuthentication compound = new CompoundAuthentication();
		compound.addReadyAuthentication(auth);
		SecurityContextHolder.getContext().setAuthentication(compound);
		entryPoint.commence(request, response, new InsufficientAuthenticationException("Insufficient!"));
		assertSame(compound, SecurityContextHolder.getContext().getAuthentication());
		
		EasyMock.verify(response);
	}
	
	private static class Step1Authentication extends EmptyAuthenticationToken {
		private static final long serialVersionUID = 1L;
	}
	
	private static class Step2Authentication extends EmptyAuthenticationToken {
		private static final long serialVersionUID = 1L;
	}
}
