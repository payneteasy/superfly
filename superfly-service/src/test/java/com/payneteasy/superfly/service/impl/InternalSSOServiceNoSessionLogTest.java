package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * An empty result of issuing a session is explained neutrally: a locked or a suspended account gets no session,
 * just as a user without roles does. The user name is sanitized like in the neighbouring lines.
 */
public class InternalSSOServiceNoSessionLogTest {

    private static final String SUBSYSTEM = "sub";

    private UserService userService;
    private InternalSSOServiceImpl service;
    private final Logger serviceLogger = (Logger) LoggerFactory.getLogger(InternalSSOServiceImpl.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        service.setLockoutStrategy(EasyMock.createNiceMock(LockoutStrategy.class));
        service.setPasswordEncoder(new PlaintextPasswordEncoder());
        service.setLegacyPasswordEncoder(new PlaintextPasswordEncoder());
        service.setSaltSource(new NullSaltSource());
        appender.start();
        serviceLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        serviceLogger.detachAppender(appender);
    }

    private List<String> warnings() {
        return appender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    @Test
    public void authenticateWithoutSession() {
        EasyMock.expect(userService.isUserAccessibleFrom("bob", SUBSYSTEM)).andReturn(true);
        EasyMock.expect(userService.authenticate(EasyMock.eq("bob"), EasyMock.eq("pass"), EasyMock.eq("pass"),
                EasyMock.eq(SUBSYSTEM), EasyMock.anyObject(String.class), EasyMock.anyObject(String.class)))
                .andReturn(new AuthSession("bob"));
        EasyMock.replay(userService);

        assertNull(service.authenticate("bob", "pass", SUBSYSTEM, null, null));

        assertEquals(List.of("No session issued for user bob (bad credentials, no roles, or the account is locked or suspended)"),
                warnings());
    }

    @Test
    public void authenticateLogsTheUserNameSanitized() {
        EasyMock.expect(userService.isUserAccessibleFrom("bob\nfake", SUBSYSTEM)).andReturn(true);
        EasyMock.expect(userService.authenticate(EasyMock.eq("bob\nfake"), EasyMock.eq("pass"), EasyMock.eq("pass"),
                EasyMock.eq(SUBSYSTEM), EasyMock.anyObject(String.class), EasyMock.anyObject(String.class)))
                .andReturn(null);
        EasyMock.replay(userService);

        assertNull(service.authenticate("bob\nfake", "pass", SUBSYSTEM, null, null));

        assertEquals(List.of("No session issued for user bob_fake (bad credentials, no roles, or the account is locked or suspended)"),
                warnings());
    }

    @Test
    public void pseudoAuthenticateWithoutSession() {
        EasyMock.expect(userService.isUserAccessibleFrom("bob", SUBSYSTEM)).andReturn(true);
        EasyMock.expect(userService.pseudoAuthenticate("bob", SUBSYSTEM)).andReturn(new AuthSession("bob"));
        EasyMock.replay(userService);

        assertNull(service.pseudoAuthenticate("bob", SUBSYSTEM));

        assertEquals(List.of("No session issued for user 'bob' during pseudo-login (no roles, or the account is locked or suspended)"),
                warnings());
    }

    @Test
    public void exchangeSubsystemTokenWithoutSession() {
        EasyMock.expect(userService.exchangeSubsystemToken("token", "caller")).andReturn(new AuthSession("bob"));
        EasyMock.replay(userService);

        assertNull(service.exchangeSubsystemToken("token", "caller"));

        assertEquals(List.of("No session issued for user bob (no roles, or the account is locked or suspended)"),
                warnings());
    }
}
