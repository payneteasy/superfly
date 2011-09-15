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
		assertEquals("user:test-user, event:create_user, resource:new-user, result:success", appender.getLastMessage());
	}
	
	public void testInfoWithNullUser() {
		loggerSink.setUserInfoService(new UserInfoServiceMock() {
			@Override
			public String getUsername() {
				return null;
			}
		});
		loggerSink.info(LoggerFactory.getLogger("testLogger"), "create_user", true, "new-user");
		assertEquals("user:<SYSTEM>, event:create_user, resource:new-user, result:success", appender.getLastMessage());
	}
	
	public void testInfoFailure() {
		loggerSink.info(LoggerFactory.getLogger("testLogger"), "create_user", false, "new-user");
		assertEquals("user:test-user, event:create_user, resource:new-user, result:failure", appender.getLastMessage());
	}
}
