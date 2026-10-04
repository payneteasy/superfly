package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

/**
 * Login paths must send both the new and the legacy hash so that SQL can rehash on success.
 */
public class LoginPassesBothHashesTest {
    private static final String LEGACY = DigestUtils.sha256Hex("pass{salt}");

    @Test
    public void internalSsoAuthenticate() {
        UserService userService = EasyMock.createStrictMock(UserService.class);
        InternalSSOServiceImpl service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(com.payneteasy.superfly.lockout.LockoutStrategy.class));
        service.setPasswordEncoder(new PlaintextPasswordEncoder());
        service.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        service.setSaltSource(new ConstantSaltSource("salt"));
        EasyMock.expect(userService.authenticate(eq("user"), eq("pass{salt}"), eq(LEGACY),
                anyObject(String.class), anyObject(String.class), anyObject(String.class))).andReturn(null);
        EasyMock.replay(userService);

        service.authenticate("user", "pass", "subsystem", null, null);

        EasyMock.verify(userService);
    }

    @Test
    public void localSecurityAuthenticate() {
        UserService userService = EasyMock.createStrictMock(UserService.class);
        LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(new NoneLockoutStrategy());
        UserPasswordEncoderImpl encoder = new UserPasswordEncoderImpl();
        encoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        encoder.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        encoder.setSaltSource(new ConstantSaltSource("salt"));
        service.setUserPasswordEncoder(encoder);
        EasyMock.expect(userService.authenticate(eq("user"), eq("pass{salt}"), eq(LEGACY),
                anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                .andReturn(new AuthSession("user"));
        EasyMock.replay(userService);

        service.authenticate("user", "pass");

        EasyMock.verify(userService);
    }
}
