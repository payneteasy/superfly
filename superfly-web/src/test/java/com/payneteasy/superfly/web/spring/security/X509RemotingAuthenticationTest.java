package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.security.SubsystemUserDetailsService;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Client-certificate authentication of subsystem RPC: an unknown CN or a subsystem without a token gets the
 * same 401 JSON as a wrong token; a known CN passes.
 */
public class X509RemotingAuthenticationTest {

    private AnnotationConfigWebApplicationContext context;
    private Filter securityFilterChain;
    private SubsystemService subsystemService;

    @Before
    public void setUp() {
        subsystemService = createMock(SubsystemService.class);
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SpringSecurityConfiguration.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", new SuperflyProperties()
                    .csrfLoginValidatorEnable(false)
                    .enableMultiFactorAuth(false));
            LoggerSink loggerSink = niceMock(LoggerSink.class);
            replay(loggerSink);
            beanFactory.registerSingleton("loggerSink", loggerSink);
            LocalSecurityService localSecurityService = niceMock(LocalSecurityService.class);
            replay(localSecurityService);
            beanFactory.registerSingleton("localSecurityService", localSecurityService);
            beanFactory.registerSingleton("userService", ActiveAccounts.userService());
            beanFactory.registerSingleton("userDetailsService",
                    new SubsystemUserDetailsService(subsystemService, loggerSink));
            beanFactory.registerSingleton("mvcHandlerMappingIntrospector", new HandlerMappingIntrospector());
        });
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void unknownCnGets401Json() throws Exception {
        expect(subsystemService.getSubsystemAuth("paynet-local")).andReturn(null);

        MockHttpServletResponse response = call("paynet-local");

        assertUnauthorizedJson(response);
    }

    @Test
    public void knownCnWithoutTokenGets401Json() throws Exception {
        SubsystemAuth auth = new SubsystemAuth();
        expect(subsystemService.getSubsystemAuth("paynet-local")).andReturn(auth);

        MockHttpServletResponse response = call("paynet-local");

        assertUnauthorizedJson(response);
    }

    @Test
    public void knownCnWithTokenPassesToChain() throws Exception {
        SubsystemAuth auth = new SubsystemAuth();
        auth.setSubsystemToken("stored-token-hash");
        expect(subsystemService.getSubsystemAuth("paynet-local")).andReturn(auth);

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = call("paynet-local", chain);

        assertNotNull("authenticated request must reach the servlet", chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void authenticationFailureBelowTheFilterIsNotSwallowed() throws Exception {
        SubsystemAuth auth = new SubsystemAuth();
        auth.setSubsystemToken("stored-token-hash");
        expect(subsystemService.getSubsystemAuth("paynet-local")).andReturn(auth);

        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) {
                throw new BadCredentialsException("rejected downstream");
            }
        });
        MockHttpServletResponse response = call("paynet-local", chain);

        assertEquals(401, response.getStatus());
    }

    private MockHttpServletResponse call(String cn) throws Exception {
        return call(cn, new MockFilterChain());
    }

    private MockHttpServletResponse call(String cn, MockFilterChain chain) throws Exception {
        replay(subsystemService);
        context.refresh();
        securityFilterChain = context.getBean("springSecurityFilterChain", Filter.class);

        X509Certificate cert = niceMock(X509Certificate.class);
        expect(cert.getSubjectX500Principal()).andStubReturn(new X500Principal("CN=" + cn + ", O=Test"));
        replay(cert);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/remoting/sso.service/getLastEventId");
        request.setServletPath("/remoting");
        request.setPathInfo("/sso.service/getLastEventId");
        request.setAttribute("jakarta.servlet.request.X509Certificate", new X509Certificate[]{cert});
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityFilterChain.doFilter(request, response, chain);
        return response;
    }

    private static void assertUnauthorizedJson(MockHttpServletResponse response) throws Exception {
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("{\"message\":\"Authentication failed\",\"status\":\"error\"}", response.getContentAsString());
        assertNull(response.getHeader("Location"));
    }
}
