package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Subsystem RPC authenticates every request by headers and must not leave a session behind.
 */
public class RemotingStatelessSecurityTest {

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
            UserDetailsService subsystems = name -> new User(name, SubsystemTokenHasher.hash("test-token"),
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
    public void authenticatedRpcDoesNotCreateSession() throws Exception {
        MockHttpServletRequest request = authenticatedRpcRequest("test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilterChain.doFilter(request, response, chain);

        assertNotNull("request must be authenticated", chain.getRequest());
        assertNull("no session may be created", request.getSession(false));
        assertNull(response.getHeader("Set-Cookie"));
    }

    @Test
    public void sessionWithoutHeadersDoesNotAuthenticateRpc() throws Exception {
        MockHttpServletRequest first = authenticatedRpcRequest("test-token");
        securityFilterChain.doFilter(first, new MockHttpServletResponse(), new MockFilterChain());
        // with the old stateful chain the security context was stored in the session created above
        MockHttpSession session = first.getSession(false) == null
                ? new MockHttpSession() : (MockHttpSession) first.getSession(false);

        MockHttpServletRequest replay = rpcRequest();
        replay.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        securityFilterChain.doFilter(replay, response, chain);

        assertNull("replay without headers must not pass", chain.getRequest());
    }

    @Test
    public void wrongTokenIsRejected() throws Exception {
        MockHttpServletRequest request = authenticatedRpcRequest("wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilterChain.doFilter(request, response, chain);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
        assertFalse(response.containsHeader("Set-Cookie"));
    }

    private static MockHttpServletRequest authenticatedRpcRequest(String token) {
        MockHttpServletRequest request = rpcRequest();
        request.addHeader("X-Subsystem-Name", "billing");
        request.addHeader("X-Subsystem-Token", token);
        return request;
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
