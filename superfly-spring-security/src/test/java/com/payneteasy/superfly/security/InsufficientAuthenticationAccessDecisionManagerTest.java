package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.exception.InsufficientAuthenticationException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.easymock.EasyMock.*;

public class InsufficientAuthenticationAccessDecisionManagerTest {

    private AccessDecisionManager delegate;
    private InsufficientAuthenticationAccessDecisionManager manager;

    @Before
    public void setUp() {
        delegate = EasyMock.createMock(AccessDecisionManager.class);
        manager = new InsufficientAuthenticationAccessDecisionManager(delegate);
        manager.setInsufficientAuthenticationClasses(new Class<?>[]{Insuf.class});
    }

    @Test
    public void testInsufficientAuthentication() {
        Insuf auth = new Insuf();
        try {
            manager.decide(auth, createFilter(), Collections.<ConfigAttribute>emptySet());
            Assert.fail();
        } catch (InsufficientAuthenticationException e) {
            // expected
            Assert.assertSame(auth, e.getAuthentication());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSufficientAuthentication() {
        delegate.decide(anyObject(Authentication.class), anyObject(), anyObject(Collection.class));
        replay(delegate);
        manager.decide(new Suf(), createFilter(), Collections.<ConfigAttribute>emptySet());
        verify(delegate);
    }

    @Test
    public void testDefaultConstructor() {
        new InsufficientAuthenticationAccessDecisionManager();
    }

    private GenericFilterBean createFilter() {
        return new GenericFilterBean() {
            public void doFilter(ServletRequest request, ServletResponse response,
                    FilterChain chain) throws IOException, ServletException {
            }
        };
    }

    private static class Insuf extends EmptyAuthenticationToken {
        private static final long serialVersionUID = 7147774578593339557L;

    }

    private static class Suf extends EmptyAuthenticationToken {
        private static final long serialVersionUID = -8651621693219389336L;

    }

}
