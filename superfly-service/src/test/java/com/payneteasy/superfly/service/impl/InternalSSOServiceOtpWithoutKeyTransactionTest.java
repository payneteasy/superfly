package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.CheckOtpResult.Status;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.hotp.HOTPServiceImpl;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Both services are transactional, and the HOTP one joins the transaction of the caller: an exception passing through
 * it must not mark that transaction rollback-only when the caller turns the exception into a result.
 * The transactions are driven by the real {@code @Transactional} attributes of the classes.
 */
public class InternalSSOServiceOtpWithoutKeyTransactionTest {

    private static final String USER = "user";
    private static final String CODE = "123456";

    private UserService userService;
    private RecordingTransactionManager transactionManager;
    private InternalSSOService internalProxy;

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        transactionManager = new RecordingTransactionManager();

        HOTPServiceImpl hotp = new HOTPServiceImpl();
        hotp.setUserService(userService);
        hotp.setCryptoService(new CryptoServiceImpl("test-secret", "test-salt"));

        InternalSSOServiceImpl internal = new InternalSSOServiceImpl();
        internal.setUserService(userService);
        internal.setHotpService(transactional(hotp, HOTPService.class));
        internal.setLockoutStrategy(EasyMock.createStrictMock(LockoutStrategy.class));
        internal.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        internalProxy = transactional(internal, InternalSSOService.class);
    }

    private <T> T transactional(T target, Class<T> type) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.setInterfaces(type);
        factory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        @SuppressWarnings("unchecked")
        T proxy = (T) factory.getProxy();
        return proxy;
    }

    private void expectAccount(String encryptedKey) {
        UserForDescription stored = new UserForDescription();
        stored.setUsername(USER);
        stored.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        UserWithStatus status = new UserWithStatus();
        status.setUserName(USER);
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(stored).anyTimes();
        EasyMock.expect(userService.getUserStatuses(USER)).andReturn(Collections.singletonList(status)).anyTimes();
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(encryptedKey).anyTimes();
        EasyMock.replay(userService);
    }

    @Test
    public void userWithoutKeyGetsInvalidAndTheTransactionIsCommitted() {
        expectAccount(null);

        assertEquals(Status.INVALID, internalProxy.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        assertEquals(Arrays.asList("begin", "commit"), transactionManager.events);
    }

    @Test
    public void keyThatCannotBeDecryptedFailsAndTheTransactionIsRolledBack() {
        expectAccount("not-a-ciphertext");

        try {
            internalProxy.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE);
            fail("a broken stored key must not be reported as a wrong code");
        } catch (SsoDecryptException e) {
            assertEquals("decrypt error", e.getMessage());
        }

        assertEquals("rollback", transactionManager.events.get(transactionManager.events.size() - 1));
        assertFalse(transactionManager.events.contains("commit"));
    }

    /** One transaction at a time is enough for the test: it records what happened to it. */
    private static class RecordingTransactionManager extends AbstractPlatformTransactionManager {

        final List<String> events = new ArrayList<>();
        private final Transaction transaction = new Transaction();

        private static class Transaction implements SmartTransactionObject {
            boolean active;
            boolean rollbackOnly;

            @Override
            public boolean isRollbackOnly() {
                return rollbackOnly;
            }

            @Override
            public void flush() {
            }
        }

        @Override
        protected Object doGetTransaction() {
            return transaction;
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) {
            return this.transaction.active;
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            this.transaction.active = true;
            this.transaction.rollbackOnly = false;
            events.add("begin");
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            transaction.active = false;
            events.add("commit");
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            transaction.active = false;
            events.add("rollback");
        }

        @Override
        protected void doSetRollbackOnly(DefaultTransactionStatus status) {
            transaction.rollbackOnly = true;
            events.add("rollback-only");
        }
    }
}
