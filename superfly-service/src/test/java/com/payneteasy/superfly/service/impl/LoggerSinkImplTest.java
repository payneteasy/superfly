package com.payneteasy.superfly.service.impl;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.slf4j.LoggerFactory;

public class LoggerSinkImplTest extends TestCase {
	
	private LoggerSinkImpl loggerSink;
	private StackAppender appender;
	
	public void setUp() {
		loggerSink = new LoggerSinkImpl();
		loggerSink.setUserInfoService(new UserInfoServiceMock());
		appender = new StackAppender();
		LogManager.getRootLogger().removeAllAppenders();
		LogManager.getRootLogger().addAppender(appender);
	}
	
	public void tearDown() {
		LogManager.getLoggerRepository().resetConfiguration();
		loggerSink = null;
		appender = null;
	}
	
	public void testInfo() {
		loggerSink.info(LoggerFactory.getLogger("testLogger"), "create_user", true, "new-user");
		assertEquals("test-user:create_user:new-user:success", appender.getLastMessage());
	}
}
