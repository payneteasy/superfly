package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.security.SubsystemAuthenticationToken;
import jakarta.servlet.Filter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unauthenticated subsystem RPC gets 401 (not a redirect to the login form); Referrer-Policy on the main chain.
 */
public class RemotingUnauthenticatedAndReferrerTest {

    private static AnnotationConfigWebApplicationContext context;
    private static Filter securityFilterChain;

    @BeforeClass
    public static void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SpringSecurityConfiguration.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", new SuperflyProperties()
                    .csrfLoginValidatorEnable(false)
                    .enableMultiFactorAuth(false));
            beanFactory.registerSingleton("loggerSink", mock(LoggerSink.class));
            beanFactory.registerSingleton("localSecurityService", mock(LocalSecurityService.class));
            UserDetailsService subsystems = name -> new User(name, "test-token",
                    List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM")));
            beanFactory.registerSingleton("userDetailsService", subsystems);
            beanFactory.registerSingleton("mvcHandlerMappingIntrospector", new HandlerMappingIntrospector());
        });
        context.refresh();
        securityFilterChain = context.getBean("springSecurityFilterChain", Filter.class);
    }

    @AfterClass
    public static void tearDown() {
        context.close();
    }

    @Test
    public void rpcWithoutCredentialsGets401NotRedirect() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilterChain.doFilter(rpcRequest(), response, chain);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
        assertNull(response.getHeader("Location"));
    }

    @Test
    public void staleSessionWithSubsystemContextDoesNotAuthenticateRpc() throws Exception {
        MockHttpSession session = new MockHttpSession();
        SecurityContextImpl stale = new SecurityContextImpl(new SubsystemAuthenticationToken("billing", "test-token",
                List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM"))));
        session.setAttribute("SPRING_SECURITY_CONTEXT", stale);
        MockHttpServletRequest request = rpcRequest();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilterChain.doFilter(request, response, chain);

        assertNull("session context must not authenticate RPC", chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    public void mainChainSendsReferrerPolicy() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sso/login");
        request.setServletPath("/sso/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityFilterChain.doFilter(request, response, new MockFilterChain());

        assertEquals("same-origin", response.getHeader("Referrer-Policy"));
    }

    private static MockHttpServletRequest rpcRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/remoting/sso.service/getUserDescription");
        request.setServletPath("/remoting");
        request.setPathInfo("/sso.service/getUserDescription");
        return request;
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
