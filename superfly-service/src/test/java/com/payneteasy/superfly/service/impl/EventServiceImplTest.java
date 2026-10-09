package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.EventDao;
import com.payneteasy.superfly.model.Event;
import com.payneteasy.superfly.service.EventService;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class EventServiceImplTest {

    private static final Long LAST = 0L;

    private static EventDao dao(java.util.function.Supplier<List<Event>> supplier) {
        return (EventDao) Proxy.newProxyInstance(EventDao.class.getClassLoader(), new Class<?>[]{EventDao.class},
                (p, m, a) -> supplier.get());
    }

    private static EventServiceImpl service(EventDao dao, AtomicLong time) {
        EventServiceImpl service = new EventServiceImpl();
        service.setEventDao(dao);
        service.clock = time::get;
        service.sleeper = time::addAndGet;
        return service;
    }

    @Test
    public void lastEventIdIsReadForTheGivenSubsystem() {
        java.util.List<Object[]> calls = new java.util.ArrayList<>();
        EventDao dao = (EventDao) Proxy.newProxyInstance(EventDao.class.getClassLoader(), new Class<?>[]{EventDao.class},
                (p, m, a) -> {
                    calls.add(a);
                    return 42L;
                });

        assertEquals(42L, service(dao, new AtomicLong()).getLastEventId("s"));
        assertEquals(1, calls.size());
        assertEquals("s", calls.get(0)[0]);
    }

    @Test
    public void cursorAndSubsystemAreForwardedToDaoOnEveryPoll() {
        java.util.List<Object[]> calls = new java.util.ArrayList<>();
        EventDao dao = (EventDao) Proxy.newProxyInstance(EventDao.class.getClassLoader(), new Class<?>[]{EventDao.class},
                (p, m, a) -> {
                    calls.add(a);
                    return List.of();
                });
        EventServiceImpl service = service(dao, new AtomicLong());

        service.getEvents(42L, 1000, "s");

        assertTrue(calls.size() > 1);
        for (Object[] args : calls) {
            assertEquals(42L, args[0]);
            assertEquals("s", args[2]);
        }
    }

    @After
    public void clearInterrupt() {
        Thread.interrupted();
    }

    @Test
    public void waitTimeIsCappedToUpperBound() {
        AtomicLong time = new AtomicLong();
        EventServiceImpl service = service(dao(List::of), time);

        assertTrue(service.getEvents(LAST, Long.MAX_VALUE / 2, "s").isEmpty());

        assertTrue(time.get() >= EventServiceImpl.MAX_WAIT_TIME_MS);
        assertTrue(time.get() < EventServiceImpl.MAX_WAIT_TIME_MS + 1000);
    }

    @Test
    public void negativeWaitTimeDoesNotWait() {
        AtomicLong time = new AtomicLong();
        EventServiceImpl service = service(dao(List::of), time);

        assertTrue(service.getEvents(LAST, -5, "s").isEmpty());

        assertEquals(0, time.get());
    }

    @Test
    public void interruptRestoresFlagAndReturns() {
        AtomicLong time = new AtomicLong();
        EventServiceImpl service = service(dao(List::of), time);
        service.sleeper = millis -> {
            throw new InterruptedException();
        };

        assertTrue(service.getEvents(LAST, 10_000, "s").isEmpty());

        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    public void notExecutedInsideTransaction() {
        AtomicBoolean firstCallTx = new AtomicBoolean(true);
        AtomicBoolean secondCallTx = new AtomicBoolean(true);
        AtomicLong calls = new AtomicLong();
        EventDao dao = dao(() -> {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            if (calls.getAndIncrement() == 0) {
                firstCallTx.set(active);
            } else {
                secondCallTx.set(active);
            }
            return List.of();
        });
        AtomicLong time = new AtomicLong();
        EventServiceImpl target = service(dao, time);

        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(EventDao.class, () -> dao);
            ctx.registerBean(EventServiceImpl.class, () -> target);
            ctx.register(TxConfig.class);
            ctx.refresh();
            EventService service = ctx.getBean(EventService.class);
            TransactionTemplate outer = new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class));

            // even when the caller already holds a transaction
            outer.executeWithoutResult(status -> {
                assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
                service.getEvents(LAST, 1000, "s");
            });
        }

        assertTrue(calls.get() >= 2);
        assertFalse(firstCallTx.get());
        assertFalse(secondCallTx.get());
    }

    /** Starts a poll on its own thread; the poll blocks in sleep until released. */
    private static Thread startBlockedPoll(EventServiceImpl service, String subsystem, CountDownLatch entered) {
        Thread t = new Thread(() -> service.getEvents(LAST, 1000, subsystem));
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static EventServiceImpl blockingService(CountDownLatch entered, CountDownLatch release, AtomicLong sleeps) {
        EventServiceImpl service = service(dao(List::of), new AtomicLong());
        service.sleeper = millis -> {
            if (millis == EventServiceImpl.OVER_LIMIT_PAUSE_MS) {
                return; // the pause of an over-limit poll must not block the test thread
            }
            sleeps.incrementAndGet();
            entered.countDown();
            release.await();
            throw new InterruptedException();
        };
        return service;
    }

    @Test
    public void excessPollsOfOneSubsystemReturnEmptyWithoutWaiting() throws Exception {
        int n = EventServiceImpl.MAX_WAITING_PER_SUBSYSTEM;
        CountDownLatch entered = new CountDownLatch(n);
        CountDownLatch release = new CountDownLatch(1);
        AtomicLong sleeps = new AtomicLong();
        EventServiceImpl service = blockingService(entered, release, sleeps);
        List<Thread> threads = new ArrayList<>();
        try {
            for (int i = 0; i < n; i++) {
                threads.add(startBlockedPoll(service, "a", entered));
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS));

            assertTrue(service.getEvents(LAST, 1000, "a").isEmpty());
            assertEquals(n, sleeps.get());

            // another subsystem is not affected
            CountDownLatch otherEntered = new CountDownLatch(1);
            threads.add(startBlockedPoll(service, "b", otherEntered));
            long deadline = System.currentTimeMillis() + 5000;
            while (sleeps.get() < n + 1 && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(n + 1, sleeps.get());
        } finally {
            release.countDown();
            for (Thread t : threads) {
                t.join(5000);
            }
        }
    }

    @Test
    public void globalLimitAppliesAcrossSubsystems() throws Exception {
        int n = EventServiceImpl.MAX_WAITING_TOTAL;
        CountDownLatch entered = new CountDownLatch(n);
        CountDownLatch release = new CountDownLatch(1);
        AtomicLong sleeps = new AtomicLong();
        EventServiceImpl service = blockingService(entered, release, sleeps);
        List<Thread> threads = new ArrayList<>();
        try {
            for (int i = 0; i < n; i++) {
                threads.add(startBlockedPoll(service, "s" + i, entered));
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS));

            assertTrue(service.getEvents(LAST, 1000, "other").isEmpty());
            assertEquals(n, sleeps.get());
        } finally {
            release.countDown();
            for (Thread t : threads) {
                t.join(5000);
            }
        }
        // permits are returned once the polls are over
        AtomicLong after = new AtomicLong();
        service.clock = after::get;
        service.sleeper = after::addAndGet;
        service.getEvents(LAST, 1, "other");
        assertEquals(500, after.get());
    }

    @Test
    public void overLimitPollPausesForOneSecondWithoutHoldingPermits() throws Exception {
        int n = EventServiceImpl.MAX_WAITING_PER_SUBSYSTEM;
        CountDownLatch entered = new CountDownLatch(n);
        CountDownLatch release = new CountDownLatch(1);
        AtomicLong sleeps = new AtomicLong();
        EventServiceImpl service = blockingService(entered, release, sleeps);
        List<Thread> threads = new ArrayList<>();
        try {
            for (int i = 0; i < n; i++) {
                threads.add(startBlockedPoll(service, "a", entered));
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS));

            List<Long> pauses = new ArrayList<>();
            service.sleeper = pauses::add;
            assertTrue(service.getEvents(LAST, 60_000, "a").isEmpty());
            assertEquals(List.of(1000L), pauses);

            pauses.clear();
            assertTrue(service.getEvents(LAST, 300, "a").isEmpty());
            assertEquals(List.of(300L), pauses);
        } finally {
            release.countDown();
            for (Thread t : threads) {
                t.join(5000);
            }
        }
    }

    @Test
    public void permitsAreReleasedWhenDaoFails() {
        AtomicLong calls = new AtomicLong();
        EventServiceImpl service = service(dao(() -> {
            if (calls.getAndIncrement() > 0) {
                throw new IllegalStateException("db down");
            }
            return List.of();
        }), new AtomicLong());

        for (int i = 0; i < EventServiceImpl.MAX_WAITING_TOTAL * 2; i++) {
            calls.set(0);
            try {
                service.getEvents(LAST, 1000, "s");
                fail();
            } catch (IllegalStateException expected) {
                // the poll was admitted and failed; the permits must be back
            }
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TxConfig {
        @Bean
        PlatformTransactionManager transactionManager() {
            DataSource ds = (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                    new Class<?>[]{DataSource.class}, (p, m, a) -> {
                        if ("hashCode".equals(m.getName())) {
                            return System.identityHashCode(p);
                        }
                        if ("equals".equals(m.getName())) {
                            return p == a[0];
                        }
                        if ("getConnection".equals(m.getName())) {
                            return Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                    new Class<?>[]{Connection.class}, (cp, cm, ca) -> {
                                        Class<?> type = cm.getReturnType();
                                        if (type == boolean.class) {
                                            return Boolean.FALSE;
                                        }
                                        return type == int.class ? (Object) 0 : null;
                                    });
                        }
                        return null;
                    });
            return new DataSourceTransactionManager(ds);
        }
    }
}
