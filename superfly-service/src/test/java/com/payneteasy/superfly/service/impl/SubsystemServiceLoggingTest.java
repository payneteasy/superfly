package com.payneteasy.superfly.service.impl;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SubsystemService;

public class SubsystemServiceLoggingTest extends AbstractServiceLoggingTest {

    private SubsystemService subsystemService;
    private SubsystemDao subsystemDao;

    @Before
    public void setUp() {
        SubsystemServiceImpl service = new SubsystemServiceImpl();
        subsystemDao = EasyMock.createStrictMock(SubsystemDao.class);
        service.setSubsystemDao(subsystemDao);
        service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
        service.setLoggerSink(loggerSink);
        service.setJavaMailSenderPool(TrivialProxyFactory.createProxy(JavaMailSenderPool.class));
        subsystemService = service;
    }

    @Test
    public void testCreateSubsystem() throws Exception {
        loggerSink.info(anyObject(Logger.class), eq("CREATE_SUBSYSTEM"), eq(true), eq("test-subsystem"));
        EasyMock.replay(loggerSink);

        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("test-subsystem");
        subsystemService.createSubsystem(subsystem);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testDeleteSubsystem() throws Exception {
        subsystemDao.deleteSubsystem(eq(1L));
        EasyMock.expectLastCall().andReturn(okResult());
        loggerSink.info(anyObject(Logger.class), eq("DELETE_SUBSYSTEM"), eq(true), eq("1"));
        EasyMock.replay(loggerSink, subsystemDao);

        subsystemService.deleteSubsystem(1L);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testDeleteSubsystemFail() throws Exception {
        subsystemDao.deleteSubsystem(eq(1L));
        EasyMock.expectLastCall().andReturn(failureResult());
        loggerSink.info(anyObject(Logger.class), eq("DELETE_SUBSYSTEM"), eq(false), eq("1"));
        EasyMock.replay(loggerSink, subsystemDao);

        subsystemService.deleteSubsystem(1L);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testUpdateSubsystem() {
        loggerSink.info(anyObject(Logger.class), eq("UPDATE_SUBSYSTEM"), eq(true), eq("test-subsystem"));
        subsystemDao.updateSubsystem(anyObject(UISubsystem.class));
        EasyMock.expectLastCall().andReturn(okResult());
        EasyMock.replay(loggerSink, subsystemDao);

        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("test-subsystem");
        subsystemService.updateSubsystem(subsystem);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testUpdateSubsystemFail() {
        loggerSink.info(anyObject(Logger.class), eq("UPDATE_SUBSYSTEM"), eq(false), eq("test-subsystem"));
        subsystemDao.updateSubsystem(anyObject(UISubsystem.class));
        EasyMock.expectLastCall().andReturn(failureResult());
        EasyMock.replay(loggerSink, subsystemDao);

        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("test-subsystem");
        subsystemService.updateSubsystem(subsystem);

        EasyMock.verify(loggerSink);
    }
}
