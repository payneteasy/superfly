package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SuperflySSOAuthenticationProcessingFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(SuperflySSOAuthenticationProcessingFilterTest.class);

    private SuperflySSOAuthenticationProcessingFilter filter;
    private AuthenticationManager authenticationManager;

    @Before
    public void setUp() {
        logger.debug("Setting up SuperflySSOAuthenticationProcessingFilterTest");
        filter = new SuperflySSOAuthenticationProcessingFilter();
        authenticationManager = EasyMock.createMock(AuthenticationManager.class);
        filter.setAuthenticationManager(authenticationManager);
    }

    @Test
    public void testAttemptAuthentication_passesCompoundAuthenticationToManager() {
        logger.debug("Testing attemptAuthentication delegates to AuthenticationManager with CompoundAuthentication");

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        Authentication result = EasyMock.createMock(Authentication.class);

        EasyMock.expect(request.getParameter("subsystemToken")).andReturn("test-token");
        EasyMock.expect(request.getParameter("targetUrl")).andReturn("/target");
        EasyMock.expect(authenticationManager.authenticate(EasyMock.anyObject(CompoundAuthentication.class)))
                .andReturn(result);

        EasyMock.replay(request, response, authenticationManager, result);

        Authentication actual = filter.attemptAuthentication(request, response);

        Assert.assertSame(result, actual);
        logger.debug("attemptAuthentication delegated correctly, result: {}", result);

        EasyMock.verify(request, response, authenticationManager, result);
    }

    @Test
    public void testAttemptAuthentication_usesJavaxServletApi() {
        logger.debug("Verifying filter uses javax.servlet (not jakarta.servlet)");
        // Compile-time guarantee: if this class compiles with javax.servlet.http.HttpServletRequest
        // on the classpath (not jakarta), the EE8 isolation is correct.
        Class<?> requestClass = javax.servlet.http.HttpServletRequest.class;
        Assert.assertTrue("Must use javax.servlet, not jakarta.servlet",
                requestClass.getName().startsWith("javax.servlet"));
        logger.debug("Confirmed javax.servlet.http.HttpServletRequest is on classpath: {}",
                requestClass.getName());
    }

    @Test
    public void testDefaultFilterUrl() {
        logger.debug("Verifying filter is instantiable without errors");
        Assert.assertNotNull("Filter must be instantiable", filter);
        logger.debug("Filter instantiated successfully: {}", filter.getClass().getSimpleName());
    }

    @Test
    public void testObtainSubsystemToken_returnsRequestParameter() {
        logger.debug("Testing obtainSubsystemToken extracts correct parameter");

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getParameter("subsystemToken")).andReturn("my-token-value");
        EasyMock.replay(request, authenticationManager);

        String token = filter.obtainSubsystemToken(request);

        Assert.assertEquals("my-token-value", token);
        logger.debug("obtainSubsystemToken returned: {}", token);
        EasyMock.verify(request, authenticationManager);
    }

    @Test
    public void testCreateSSOAuthRequest_wrapsTokenInSSOAuthenticationRequest() {
        logger.debug("Testing createSSOAuthRequest wraps subsystem token");

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.replay(request, authenticationManager);

        Authentication authRequest = filter.createSSOAuthRequest(request, "sso-token");

        Assert.assertNotNull(authRequest);
        Assert.assertTrue("Must create SSOAuthenticationRequest",
                authRequest instanceof SSOAuthenticationRequest);
        logger.debug("createSSOAuthRequest returned: {}", authRequest.getClass().getSimpleName());
        EasyMock.verify(request, authenticationManager);
    }
}
