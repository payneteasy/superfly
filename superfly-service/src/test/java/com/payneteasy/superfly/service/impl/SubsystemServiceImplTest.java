package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rpuch
 */
public class SubsystemServiceImplTest {
    private SubsystemDao subsystemDao;
    private SubsystemServiceImpl subsystemService;

    @Before
    public void setUp() {
        subsystemDao = EasyMock.createStrictMock(SubsystemDao.class);
        subsystemService = new SubsystemServiceImpl();
        subsystemService.setSubsystemDao(subsystemDao);
    }

    @Test
    public void testGetSubsystemTokenIfCanLogin() throws Exception {
        SubsystemTokenData tokenData = new SubsystemTokenData();
        tokenData.setSubsystemToken("abc");
        tokenData.setLandingUrl("url");
        EasyMock.expect(subsystemDao.issueSubsystemTokenIfCanLogin(EasyMock.eq(1L), EasyMock.eq("subsystem"), EasyMock.anyObject(String.class)))
                .andReturn(tokenData);

        EasyMock.replay(subsystemDao);
        Assert.assertSame(tokenData, subsystemService.issueSubsystemTokenIfCanLogin(1L, "subsystem"));
        EasyMock.verify(subsystemDao);
    }

    @Test
    public void testGenerateMainSubsystemTokenStoresOnlyTheHash() {
        subsystemService.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        UISubsystem subsystem = new UISubsystem();

        String raw = subsystemService.generateMainSubsystemToken(subsystem);

        Assert.assertNotEquals(raw, subsystem.getSubsystemToken());
        Assert.assertEquals(SubsystemTokenHasher.hash(raw), subsystem.getSubsystemToken());
    }

    @Test
    public void testGenerateMainSubsystemTokenIsAudited() {
        LoggerSink loggerSink = EasyMock.createStrictMock(LoggerSink.class);
        subsystemService.setLoggerSink(loggerSink);
        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("billing");
        // the raw token must not reach the audit log: only the subsystem name is expected
        loggerSink.info(EasyMock.anyObject(org.slf4j.Logger.class), EasyMock.eq("GENERATE_SUBSYSTEM_TOKEN"),
                EasyMock.eq(true), EasyMock.eq("billing"));
        EasyMock.replay(loggerSink);

        subsystemService.generateMainSubsystemToken(subsystem);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testCreateSubsystemWritesHashToDao() {
        subsystemService.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        subsystemService.setNotificationService(EasyMock.createNiceMock(com.payneteasy.superfly.service.NotificationService.class));
        subsystemService.setJavaMailSenderPool(EasyMock.createNiceMock(com.payneteasy.superfly.service.JavaMailSenderPool.class));
        UISubsystem subsystem = new UISubsystem();
        String raw = subsystemService.generateMainSubsystemToken(subsystem);
        EasyMock.expect(subsystemDao.createSubsystem(EasyMock.same(subsystem))).andReturn(RoutineResult.okResult());
        EasyMock.replay(subsystemDao);

        subsystemService.createSubsystem(subsystem);

        EasyMock.verify(subsystemDao);
        Assert.assertEquals(SubsystemTokenHasher.hash(raw), subsystem.getSubsystemToken());
    }

    @Test
    public void testCreateSubsystemWithoutTokenStillStoresAHash() {
        subsystemService.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        subsystemService.setNotificationService(EasyMock.createNiceMock(com.payneteasy.superfly.service.NotificationService.class));
        subsystemService.setJavaMailSenderPool(EasyMock.createNiceMock(com.payneteasy.superfly.service.JavaMailSenderPool.class));
        UISubsystem subsystem = new UISubsystem();
        EasyMock.expect(subsystemDao.createSubsystem(EasyMock.same(subsystem))).andReturn(RoutineResult.okResult());
        EasyMock.replay(subsystemDao);

        subsystemService.createSubsystem(subsystem);

        Assert.assertTrue(subsystem.getSubsystemToken().startsWith(SubsystemTokenHasher.PREFIX));
    }
}
