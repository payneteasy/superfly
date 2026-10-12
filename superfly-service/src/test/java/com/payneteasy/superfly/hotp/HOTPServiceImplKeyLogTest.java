package com.payneteasy.superfly.hotp;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * A user without a key is an ordinary case and is not logged as an error; a key that cannot be decrypted is a failure
 * of the configuration and is. The user name is sanitized.
 */
public class HOTPServiceImplKeyLogTest {

    private static final String USER = "us\ner";
    private static final String CODE = "123456";

    private final Logger hotpLogger = (Logger) LoggerFactory.getLogger(HOTPServiceImpl.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level levelBefore;
    private UserService userService;
    private HOTPServiceImpl service;

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        service = new HOTPServiceImpl();
        service.setUserService(userService);
        service.setCryptoService(new CryptoServiceImpl("test-secret", "test-salt"));
        levelBefore = hotpLogger.getLevel();
        hotpLogger.setLevel(Level.DEBUG);
        appender.start();
        hotpLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        hotpLogger.detachAppender(appender);
        hotpLogger.setLevel(levelBefore);
    }

    private List<String> messages(Level level) {
        return appender.list.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    @Test
    public void missingKeyIsNotLoggedAsAnError() {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(null);
        EasyMock.replay(userService);

        try {
            service.validateGoogleTimePassword(USER, CODE);
            fail("a missing key is reported to the caller");
        } catch (SsoDecryptException e) {
            assertEquals("GA master key for " + USER + " is null", e.getMessage());
        }

        assertEquals(List.of(), messages(Level.ERROR));
        assertEquals(List.of(), messages(Level.WARN));
        assertEquals(List.of("GA master key for us_er is null"), messages(Level.DEBUG));
    }

    @Test
    public void keyThatCannotBeDecryptedIsLoggedAsAnError() {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn("not-a-ciphertext");
        EasyMock.replay(userService);

        try {
            service.validateGoogleTimePassword(USER, CODE);
            fail("a broken key must not be reported as a wrong code");
        } catch (SsoDecryptException e) {
            assertEquals("decrypt error", e.getMessage());
        }

        assertEquals(List.of("Cannot decrypt the OTP master key of user us_er"), messages(Level.ERROR));
    }
}
