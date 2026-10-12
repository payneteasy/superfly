package com.payneteasy.superfly.web.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class DefaultAdminPasswordStartupCheckTest {

    private static final String DEFAULT_HASH = "0d7d1771e08bc48f6fe90b14a89c505d344a0f6f1a54de3b10a93466cb235f96";
    private static final String DEFAULT_SALT = "3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f";

    private final PasswordEncoder legacy = new MessageDigestPasswordEncoder();
    private final Logger          log    = (Logger) LoggerFactory.getLogger(DefaultAdminPasswordStartupCheck.class);
    private ListAppender<ILoggingEvent> appender;
    private UserService userService;

    @Before
    public void setUp() {
        appender = new ListAppender<>();
        appender.start();
        log.addAppender(appender);
        userService = createMock(UserService.class);
    }

    @After
    public void tearDown() {
        log.detachAppender(appender);
    }

    private void run(List<PasswordSaltPair> stored) {
        expect(userService.getUserPasswordHistoryAndCurrentPassword("admin")).andReturn(stored);
        replay(userService);
        new DefaultAdminPasswordStartupCheck(userService, legacy).afterSingletonsInstantiated();
    }

    private static PasswordSaltPair pair(String password, String salt) {
        PasswordSaltPair p = new PasswordSaltPair();
        p.setPassword(password);
        p.setSalt(salt);
        return p;
    }

    @Test
    public void logsErrorForLegacyDefaultPassword() {
        run(Collections.singletonList(pair(DEFAULT_HASH, DEFAULT_SALT)));
        assertEquals(1, appender.list.size());
        ILoggingEvent event = appender.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        assertFalse(event.getFormattedMessage().contains("123admin123"));
        assertFalse(event.getFormattedMessage().contains(DEFAULT_HASH));
    }

    @Test
    public void logsErrorForPbkdf2DefaultPassword() {
        run(Collections.singletonList(pair(new Pbkdf2PasswordEncoder().encode("123admin123", "somesalt"), "somesalt")));
        assertEquals(Level.ERROR, appender.list.get(0).getLevel());
    }

    @Test
    public void silentForChangedPassword() {
        run(Collections.singletonList(pair(legacy.encode("another-Passw0rd", DEFAULT_SALT), DEFAULT_SALT)));
        assertTrue(appender.list.isEmpty());
    }

    @Test
    public void defaultPasswordInHistoryOnlyIsIgnored() {
        run(java.util.Arrays.asList(pair(legacy.encode("another-Passw0rd", "s2"), "s2"), pair(DEFAULT_HASH, DEFAULT_SALT)));
        assertTrue(appender.list.isEmpty());
    }

    @Test
    public void missingAdminIsSilent() {
        run(Collections.<PasswordSaltPair>emptyList());
        assertTrue(appender.list.isEmpty());
    }

    @Test
    public void databaseFailureOnlyWarns() {
        expect(userService.getUserPasswordHistoryAndCurrentPassword("admin")).andThrow(new IllegalStateException("db down"));
        replay(userService);
        new DefaultAdminPasswordStartupCheck(userService, legacy).afterSingletonsInstantiated();
        assertEquals(Level.WARN, appender.list.get(0).getLevel());
    }
}
