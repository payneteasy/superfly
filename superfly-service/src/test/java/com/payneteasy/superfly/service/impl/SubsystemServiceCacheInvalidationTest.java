package com.payneteasy.superfly.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;

public class SubsystemServiceCacheInvalidationTest {

    private SubsystemDao dao;
    private SubsystemOriginCache originCache;
    private SubsystemServiceImpl service;

    @Before
    public void setUp() {
        dao = EasyMock.createNiceMock(SubsystemDao.class);
        originCache = EasyMock.createStrictMock(SubsystemOriginCache.class);
        service = new SubsystemServiceImpl();
        service.setSubsystemDao(dao);
        service.setSubsystemOriginCache(originCache);
        service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
        service.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        service.setJavaMailSenderPool(TrivialProxyFactory.createProxy(JavaMailSenderPool.class));
    }

    @Test
    public void createInvalidates() {
        EasyMock.expect(dao.createSubsystem(EasyMock.anyObject())).andReturn(RoutineResult.okResult());
        originCache.invalidate();
        EasyMock.replay(dao, originCache);
        service.createSubsystem(new UISubsystem());
        EasyMock.verify(originCache);
    }

    @Test
    public void updateInvalidates() {
        EasyMock.expect(dao.updateSubsystem(EasyMock.anyObject())).andReturn(RoutineResult.okResult());
        originCache.invalidate();
        EasyMock.replay(dao, originCache);
        service.updateSubsystem(new UISubsystem());
        EasyMock.verify(originCache);
    }

    @Test
    public void deleteInvalidates() {
        EasyMock.expect(dao.deleteSubsystem(1L)).andReturn(RoutineResult.okResult());
        originCache.invalidate();
        EasyMock.replay(dao, originCache);
        service.deleteSubsystem(1L);
        EasyMock.verify(originCache);
    }

    @Test
    public void notInvalidatedWhenDaoFails() {
        EasyMock.expect(dao.updateSubsystem(EasyMock.anyObject())).andThrow(new IllegalStateException("boom"));
        EasyMock.replay(dao, originCache);
        try {
            service.updateSubsystem(new UISubsystem());
        } catch (IllegalStateException expected) {
            // no invalidate() expected: strict mock fails verify otherwise
        }
        EasyMock.verify(originCache);
    }

    @Test
    public void invalidatedOnlyAfterCommitInsideTransaction() {
        AtomicInteger invalidations = new AtomicInteger();
        service.setSubsystemOriginCache(new SubsystemOriginCache() {
            @Override
            public void invalidate() {
                invalidations.incrementAndGet();
            }
        });
        EasyMock.expect(dao.updateSubsystem(EasyMock.anyObject())).andReturn(RoutineResult.okResult());
        EasyMock.replay(dao);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.updateSubsystem(new UISubsystem());
            assertEquals(0, invalidations.get());
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertEquals(1, invalidations.get());
    }
}
