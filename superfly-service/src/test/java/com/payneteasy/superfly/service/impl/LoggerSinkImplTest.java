package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class LoggerSinkImplTest {

    private LoggerSinkImpl loggerSink;
    private Logger logger;
    private StackAppender appender;

    @Before
    public void setUp() {
        loggerSink = new LoggerSinkImpl();
        loggerSink.setUserInfoService(new UserInfoServiceMock());

        appender = new StackAppender();
        appender.start();

        logger = (Logger) LoggerFactory.getLogger("testLogger");
        logger.addAppender(appender);
    }

    @After
    public void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    public void testInfo() {
        loggerSink.info(logger, "create_user", true, "new-user");
        assertEquals("user:test-user, event:create_user, resource:new-user, result:success",
                appender.getLastMessage());
    }

    @Test
    public void testInfoWithNullUser() {
        loggerSink.setUserInfoService(new UserInfoServiceMock() {
            @Override
            public String getUsername() {
                return null;
            }
        });
        loggerSink.info(logger, "create_user", true, "new-user");
        assertEquals("user:<SYSTEM>, event:create_user, resource:new-user, result:success", appender.getLastMessage());
    }

    @Test
    public void testInfoFailure() {
        loggerSink.info(logger, "create_user", false, "new-user");
        assertEquals("user:test-user, event:create_user, resource:new-user, result:failure", appender.getLastMessage());
    }
}
