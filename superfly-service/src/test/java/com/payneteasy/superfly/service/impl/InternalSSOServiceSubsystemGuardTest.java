package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.service.impl.remote.check.RemoteAuthServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * authenticate / pseudoAuthenticate only touch users of the calling subsystem: a denial must not reach the
 * failed-login counter and must cost the same hashing as a regular attempt.
 */
public class InternalSSOServiceSubsystemGuardTest {

    private static final String LOCAL  = "superfly";
    private static final String CALLER = "billing";
    private static final String USER   = "victim";

    private UserService userService;
    private LockoutStrategy lockoutStrategy;
    private AtomicInteger encodeCalls;
    private InternalSSOServiceImpl service;

    @Before
    public void setUp() {
        userService = createStrictMock(UserService.class);
        lockoutStrategy = createStrictMock(LockoutStrategy.class);
        encodeCalls = new AtomicInteger();
        PasswordEncoder countingEncoder = (password, salt) -> {
            encodeCalls.incrementAndGet();
            return password + "{" + salt + "}";
        };
        service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(lockoutStrategy);
        service.setPasswordEncoder(countingEncoder);
        service.setLegacyPasswordEncoder(countingEncoder);
        service.setSaltSource(new ConstantSaltSource("salt"));
    }

    private void expectForeign() {
        expect(userService.isUserAccessibleFrom(USER, CALLER)).andReturn(false);
    }

    private void expectLocalUser() {
        expect(userService.isUserAccessibleFrom(USER, CALLER)).andReturn(false);
    }

    private void expectOwn() {
        expect(userService.isUserAccessibleFrom(USER, CALLER)).andReturn(true);
    }

    private static AuthSession session() {
        AuthSession session = new AuthSession(USER, 1L);
        session.setRoles(Collections.singletonList(new AuthRole("role")));
        return session;
    }

    @Test
    public void authenticateForeignUserReturnsNullWithoutTouchingCounter() {
        expectForeign();
        replay(userService, lockoutStrategy);

        assertNull(service.authenticate(USER, "pass", CALLER, null, null));

        verify(userService, lockoutStrategy);
    }

    @Test
    public void authenticateLocalUserReturnsNullWithoutTouchingCounter() {
        expectLocalUser();
        replay(userService, lockoutStrategy);

        assertNull(service.authenticate(USER, "pass", CALLER, null, null));

        verify(userService, lockoutStrategy);
    }

    @Test
    public void authenticateDeniedUserStillPaysForHashing() {
        expectForeign();
        replay(userService, lockoutStrategy);

        service.authenticate(USER, "pass", CALLER, null, null);

        // both hashes, like the regular path
        assertEquals(2, encodeCalls.get());
    }

    @Test
    public void authenticateDeniedUserWithNullPasswordDoesNotHash() {
        expectForeign();
        replay(userService, lockoutStrategy);

        assertNull(service.authenticate(USER, null, CALLER, null, null));

        assertEquals(0, encodeCalls.get());
        verify(userService, lockoutStrategy);
    }

    @Test
    public void authenticateWithoutSubsystemIsDenied() {
        expect(userService.isUserAccessibleFrom(USER, null)).andReturn(false);
        replay(userService, lockoutStrategy);

        assertNull(service.authenticate(USER, "pass", null, null, null));

        assertEquals(2, encodeCalls.get());
        verify(userService, lockoutStrategy);
    }

    @Test
    public void authenticateOwnUserIsDelegated() {
        expectOwn();
        expect(userService.authenticate(eq(USER), eq("pass{salt}"), eq("pass{salt}"), anyObject(String.class),
                anyObject(String.class), anyObject(String.class))).andReturn(session());
        replay(userService, lockoutStrategy);

        SSOUser user = service.authenticate(USER, "pass", CALLER, null, null);

        assertEquals(USER, user.getName());
        verify(userService, lockoutStrategy);
    }

    @Test
    public void authenticateOwnUserWithWrongPasswordStillCountsFailure() {
        expectOwn();
        expect(userService.authenticate(eq(USER), anyObject(String.class), anyObject(String.class),
                anyObject(String.class), anyObject(String.class), anyObject(String.class))).andReturn(null);
        lockoutStrategy.checkLoginsFailed(USER, com.payneteasy.superfly.model.LockoutType.PASSWORD);
        replay(userService, lockoutStrategy);

        assertNull(service.authenticate(USER, "bad", CALLER, null, null));

        verify(userService, lockoutStrategy);
    }

    @Test
    public void pseudoAuthenticateForeignUserReturnsNull() {
        expectForeign();
        replay(userService);

        assertNull(service.pseudoAuthenticate(USER, CALLER));

        verify(userService);
    }

    @Test
    public void pseudoAuthenticateLocalUserReturnsNull() {
        expectLocalUser();
        replay(userService);

        assertNull(service.pseudoAuthenticate(USER, CALLER));

        verify(userService);
    }

    @Test
    public void pseudoAuthenticateWithoutSubsystemIsDenied() {
        expect(userService.isUserAccessibleFrom(USER, null)).andReturn(false);
        replay(userService);

        assertNull(service.pseudoAuthenticate(USER, null));

        verify(userService);
    }

    @Test
    public void pseudoAuthenticateOwnUserIsDelegated() {
        expectOwn();
        expect(userService.pseudoAuthenticate(USER, CALLER)).andReturn(session());
        replay(userService);

        assertEquals(USER, service.pseudoAuthenticate(USER, CALLER).getName());

        verify(userService);
    }

    @Test
    public void remoteAuthCheckPasswordForForeignUserGivesGenericAnswer() throws Exception {
        SubsystemService subsystemService = createNiceMock(SubsystemService.class);
        SubsystemAuth subsystem = new SubsystemAuth();
        subsystem.setName(CALLER);
        subsystem.setSubsystemToken(SubsystemTokenHasher.hash("bearer"));
        subsystem.setEncryptionAlgorithm("RSA");
        expect(subsystemService.getSubsystemAuth(CALLER)).andReturn(subsystem);
        expect(subsystemService.getSubsystemPrivateKey(CALLER)).andReturn("private-key");
        RemoteAuthCryptoService crypto = createNiceMock(RemoteAuthCryptoService.class);
        expect(crypto.decryptPassword(anyString(), anyString(), anyObject())).andReturn("pass");
        expectForeign();
        replay(subsystemService, crypto, userService, lockoutStrategy);

        try {
            new RemoteAuthServiceImpl(subsystemService, service, crypto)
                    .checkPassword(CALLER, USER, "enc", "bearer", "127.0.0.1", "test");
            fail("foreign user must be rejected");
        } catch (RemoteAuthException e) {
            assertEquals("BAD_USER_OR_PASSWORD_OR_OTP", e.getErrorCode());
        }
        verify(userService, lockoutStrategy);
    }
}
