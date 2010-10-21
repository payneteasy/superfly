package com.payneteasy.superfly.service.impl;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import org.easymock.EasyMock;
import org.slf4j.Logger;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.SyslogService;

public class SubsystemServiceLoggingTest extends AbstractServiceLoggingTest {
	
	private SubsystemService subsystemService;
	private SubsystemDao subsystemDao;
	
	public void setUp() {
		super.setUp();
		SubsystemServiceImpl service = new SubsystemServiceImpl();
		subsystemDao = EasyMock.createStrictMock(SubsystemDao.class);
		service.setSubsystemDao(subsystemDao);
		service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
		service.setLoggerSink(loggerSink);
		service.setSyslogService(TrivialProxyFactory.createProxy(SyslogService.class));
		subsystemService = service;
	}
	
	public void testCreateSubsystem() throws Exception {
		loggerSink.info(anyObject(Logger.class), eq("CREATE_SUBSYSTEM"), eq(true), eq("test-subsystem"));
		EasyMock.replay(loggerSink);
		
		UISubsystem subsystem = new UISubsystem();
		subsystem.setName("test-subsystem");
		subsystemService.createSubsystem(subsystem);
		
		EasyMock.verify(loggerSink);
	}
	
	public void testDeleteSubsystem() throws Exception {
		subsystemDao.deleteSubsystem(eq(1L));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("DELETE_SUBSYSTEM"), eq(true), eq("1"));
		EasyMock.replay(loggerSink, subsystemDao);
		
		subsystemService.deleteSubsystem(1L);
		
		EasyMock.verify(loggerSink);
	}
	
	public void testDeleteSubsystemFail() throws Exception {
		subsystemDao.deleteSubsystem(eq(1L));
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("DELETE_SUBSYSTEM"), eq(false), eq("1"));
		EasyMock.replay(loggerSink, subsystemDao);
		
		subsystemService.deleteSubsystem(1L);
		
		EasyMock.verify(loggerSink);
	}
	
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
