package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.User;
import com.payneteasy.superfly.policy.account.pcidss.PCIDSSAccountPolicy;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * The batch jobs must commit each user separately: an event is visible to pollers only after commit,
 * and a long batch transaction makes events be committed out of id order.
 */
public class UserServiceImplBatchTransactionTest {

    /** Hands out a new transaction id per outermost transaction; NOT_SUPPORTED means "no transaction". */
    private static class RecordingTransactionManager implements PlatformTransactionManager {
        Integer current;
        int lastId;
        int commits;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED
                    || current != null) {
                return new SimpleTransactionStatus(false);
            }
            current = ++lastId;
            return new SimpleTransactionStatus(true);
        }

        @Override
        public void commit(TransactionStatus status) {
            if (status.isNewTransaction()) {
                current = null;
                commits++;
            }
        }

        @Override
        public void rollback(TransactionStatus status) {
            if (status.isNewTransaction()) {
                current = null;
            }
        }
    }

    private RecordingTransactionManager txManager;
    private UserDao userDao;
    private UserService proxy;
    private final List<Integer> txIdOfSuspend = new ArrayList<>();
    private final List<Integer> txIdOfReset = new ArrayList<>();

    @Before
    public void setUp() {
        txManager = new RecordingTransactionManager();
        userDao = EasyMock.createMock(UserDao.class);
        UserServiceImpl impl = new UserServiceImpl();
        impl.setUserDao(userDao);
        impl.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        impl.setNotificationService(EasyMock.createNiceMock(NotificationService.class));

        ProxyFactory factory = new ProxyFactory(impl);
        factory.addAdvice(new TransactionInterceptor(txManager, new AnnotationTransactionAttributeSource()));
        proxy = (UserService) factory.getProxy();
        impl.setSelf(proxy);

        PCIDSSAccountPolicy policy = new PCIDSSAccountPolicy();
        policy.setUserService(proxy);
        // as in production: the strategy goes through the user service proxy
        policy.setResetPasswordStrategy(new ResetPasswordStrategy() {
            @Override
            public void resetPassword(long userId, String username, String password) {
                proxy.resetPassword(userId, null);
            }
        });
        impl.setAccountPolicy(policy);
    }

    @Test
    public void testSuspendUsersCommitsEachUserSeparately() {
        EasyMock.expect(userDao.getUsersToSuspend(30)).andReturn(List.of(user(1), user(2), user(3)));
        for (long id = 1; id <= 3; id++) {
            EasyMock.expect(userDao.suspendUser(id)).andAnswer(() -> {
                txIdOfSuspend.add(txManager.current);
                return RoutineResult.okResult();
            });
        }
        EasyMock.replay(userDao);

        proxy.suspendUsers(30);

        EasyMock.verify(userDao);
        assertEquals(3, txIdOfSuspend.size());
        txIdOfSuspend.forEach(id -> assertNotNull("suspendUser must run in a transaction", id));
        assertEquals("each user in its own transaction", 3, txIdOfSuspend.stream().distinct().count());
    }

    @Test
    public void testExpirePasswordsCommitsEachUserSeparately() {
        EasyMock.expect(userDao.getUsersWithExpiredPasswords(90)).andReturn(List.of(user(1), user(2)));
        for (long id = 1; id <= 2; id++) {
            EasyMock.expect(userDao.resetPassword(EasyMock.eq(id), EasyMock.isNull())).andAnswer(() -> {
                txIdOfReset.add(txManager.current);
                return RoutineResult.okResult();
            });
        }
        EasyMock.replay(userDao);

        proxy.expirePasswords(90);

        EasyMock.verify(userDao);
        assertEquals(2, txIdOfReset.size());
        assertNotNull(txIdOfReset.get(0));
        assertNotEquals(txIdOfReset.get(0), txIdOfReset.get(1));
    }

    @Test
    public void testFailureOfOneUserDoesNotStopOthersNorRollbackThem() {
        EasyMock.expect(userDao.getUsersToSuspend(30)).andReturn(List.of(user(1), user(2), user(3)));
        EasyMock.expect(userDao.suspendUser(1L)).andReturn(RoutineResult.okResult());
        EasyMock.expect(userDao.suspendUser(2L)).andThrow(new IllegalStateException("db failure for one user"));
        EasyMock.expect(userDao.suspendUser(3L)).andReturn(RoutineResult.okResult());
        EasyMock.replay(userDao);

        proxy.suspendUsers(30);

        EasyMock.verify(userDao);
        // the user list read + users 1 and 3 committed, user 2 rolled back
        assertEquals(3, txManager.commits);
        assertNull(txManager.current);
    }

    private static User user(long id) {
        User user = new User();
        user.setUserid(id);
        user.setUserName("user" + id);
        return user;
    }
}
