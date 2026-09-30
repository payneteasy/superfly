package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.security.SecurityAuditApplicationListener;
import com.payneteasy.superfly.web.security.SubsystemAuthenticationToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

/**
 * PCI-аудит (ревью PR #125): события аутентификации подсистем должны доходить
 * до {@link SecurityAuditApplicationListener} через настоящий AuthenticationManager.
 */
public class SecurityAuditWiringTest {

    private LoggerSink loggerSink;
    private AnnotationConfigApplicationContext context;
    private AuthenticationManager authenticationManager;

    @Before
    public void setUp() {
        loggerSink = createMock(LoggerSink.class);
        UserDetailsService userDetailsService = createMock(UserDetailsService.class);
        expect(userDetailsService.loadUserByUsername("billing"))
                .andStubReturn(new User("billing", "valid-token", List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM"))));
        replay(userDetailsService);

        context = new AnnotationConfigApplicationContext();
        context.register(SpringSecurityAuthenticationManagerConfiguration.class, SecurityAuditApplicationListener.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", new SuperflyProperties().enableMultiFactorAuth(false));
            beanFactory.registerSingleton("localSecurityService", niceMock(LocalSecurityService.class));
            beanFactory.registerSingleton("userDetailsService", userDetailsService);
            beanFactory.registerSingleton("loggerSink", loggerSink);
        });
        context.refresh();
        authenticationManager = context.getBean(AuthenticationManager.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.7");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        context.close();
    }

    @Test
    public void successfulSubsystemAuthenticationIsAudited() {
        loggerSink.info(anyObject(Logger.class), eq("SUBSYSTEM_AUTH"), eq(true), eq("subsystem=billing"));
        replay(loggerSink);

        authenticationManager.authenticate(new SubsystemAuthenticationToken("billing", "valid-token"));

        verify(loggerSink);
    }

    @Test
    public void failedSubsystemAuthenticationIsAudited() {
        loggerSink.info(anyObject(Logger.class), eq("SUBSYSTEM_AUTH"), eq(false), eq("subsystem=billing"));
        replay(loggerSink);

        try {
            authenticationManager.authenticate(new SubsystemAuthenticationToken("billing", "wrong-token"));
            fail("wrong token must be rejected");
        } catch (BadCredentialsException expected) {
            // событие публикуется до проброса исключения
        }

        verify(loggerSink);
    }
}
