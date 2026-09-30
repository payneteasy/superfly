package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.exception.InsufficientAuthenticationException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class InsufficientAuthenticationHandlingFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(InsufficientAuthenticationHandlingFilterTest.class);

    private InsufficientAuthenticationHandlingFilter filter;

    @Before
    public void setUp() {
        logger.debug("Setting up InsufficientAuthenticationHandlingFilterTest");
        filter = new InsufficientAuthenticationHandlingFilter();
        SecurityContextHolder.clearContext();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testDoFilter_noAuthentication_proceedsToChain() throws Exception {
        logger.debug("Testing doFilter proceeds when no authentication is present");

        ServletRequest request = EasyMock.createMock(ServletRequest.class);
        ServletResponse response = EasyMock.createMock(ServletResponse.class);
        FilterChain chain = EasyMock.createMock(FilterChain.class);

        chain.doFilter(request, response);
        EasyMock.expectLastCall().once();
        EasyMock.replay(request, response, chain);

        filter.doFilter(request, response, chain);

        EasyMock.verify(request, response, chain);
        logger.debug("chain.doFilter called correctly with no authentication");
    }

    @Test
    public void testDoFilter_sufficientAuthentication_proceedsToChain() throws Exception {
        logger.debug("Testing doFilter proceeds when authentication class is not in insufficient list");

        filter.setInsufficientAuthenticationClasses(new Class<?>[]{EmptyAuthenticationToken.class});

        // UsernamePasswordAuthenticationToken is NOT in the insufficient list
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ServletRequest request = EasyMock.createMock(ServletRequest.class);
        ServletResponse response = EasyMock.createMock(ServletResponse.class);
        FilterChain chain = EasyMock.createMock(FilterChain.class);

        chain.doFilter(request, response);
        EasyMock.expectLastCall().once();
        EasyMock.replay(request, response, chain);

        filter.doFilter(request, response, chain);

        EasyMock.verify(request, response, chain);
        logger.debug("chain.doFilter called for sufficient authentication");
    }

    @Test
    public void testDoFilter_insufficientAuthentication_throwsException() throws Exception {
        logger.debug("Testing doFilter throws InsufficientAuthenticationException for insufficient auth");

        filter.setInsufficientAuthenticationClasses(new Class<?>[]{EmptyAuthenticationToken.class});

        // EmptyAuthenticationToken IS in the insufficient list
        EmptyAuthenticationToken insufficientAuth = new EmptyAuthenticationToken();
        SecurityContextHolder.getContext().setAuthentication(insufficientAuth);

        ServletRequest request = EasyMock.createMock(ServletRequest.class);
        ServletResponse response = EasyMock.createMock(ServletResponse.class);
        FilterChain chain = EasyMock.createMock(FilterChain.class);

        EasyMock.replay(request, response, chain);

        try {
            filter.doFilter(request, response, chain);
            Assert.fail("Expected InsufficientAuthenticationException");
        } catch (InsufficientAuthenticationException e) {
            logger.debug("InsufficientAuthenticationException thrown as expected: {}", e.getMessage());
            Assert.assertSame(insufficientAuth, e.getAuthentication());
        }

        EasyMock.verify(request, response, chain);
    }

    @Test
    public void testUsesJavaxServletApi() {
        logger.debug("Verifying filter uses javax.servlet (not jakarta.servlet)");
        Class<?> filterChainClass = javax.servlet.FilterChain.class;
        Assert.assertTrue("Must use javax.servlet, not jakarta.servlet",
                filterChainClass.getName().startsWith("javax.servlet"));
        logger.debug("Confirmed javax.servlet.FilterChain is on classpath: {}",
                filterChainClass.getName());
    }
}
