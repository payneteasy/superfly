package com.payneteasy.superfly.security;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

public class InsufficientAuthenticationHandlingFilterTest extends AbstractFilterTest {
    @Before
    public void setUp() {
        InsufficientAuthenticationHandlingFilter insufFilter = new InsufficientAuthenticationHandlingFilter();
        insufFilter.setInsufficientAuthenticationClasses(new Class<?>[]{Insuf.class});
        filter = insufFilter;
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testThrowing() throws Exception {
        Insuf auth = new Insuf();
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            filter.doFilter(request, response, chain);
            Assert.fail();
        } catch (InsufficientAuthenticationException e) {
            // expected
            Assert.assertSame(auth, e.getAuthentication());
        }
    }

    @Test
    public void testNotThrowing() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new Suf());
        chain.doFilter(request, response);
        EasyMock.expectLastCall();
        EasyMock.replay(chain);
        filter.doFilter(request, response, chain);
    }

    @Test
    public void testNullAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        chain.doFilter(request, response);
        EasyMock.expectLastCall();
        EasyMock.replay(chain);
        filter.doFilter(request, response, chain);
    }

    private static class Insuf extends EmptyAuthenticationToken {
        private static final long serialVersionUID = 1L;
    }

    private static class Suf extends EmptyAuthenticationToken {
        private static final long serialVersionUID = 1L;
    }
}
