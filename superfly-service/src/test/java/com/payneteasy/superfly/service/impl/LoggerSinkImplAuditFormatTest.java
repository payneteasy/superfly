package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.service.UserInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class LoggerSinkImplAuditFormatTest {

    private LoggerSinkImpl loggerSink;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private volatile String actor = "test-user";
    private volatile String ip = "192.0.2.7";

    @Before
    public void setUp() {
        loggerSink = new LoggerSinkImpl();
        loggerSink.setUserInfoService(new UserInfoService() {
            public String getUsername() {
                return actor;
            }

            @Override
            public String getRemoteAddress() {
                return ip;
            }
        });
        appender = new ListAppender<>();
        appender.start();
        logger = (Logger) LoggerFactory.getLogger("auditFormatTestLogger");
        logger.addAppender(appender);
    }

    @After
    public void tearDown() {
        logger.detachAppender(appender);
    }

    private String last() {
        return appender.list.get(appender.list.size() - 1).getFormattedMessage();
    }

    @Test
    public void ipIsAppended() {
        loggerSink.info(logger, "EVT", true, "bob");
        assertEquals("user:test-user, event:EVT, resource:bob, result:success, ip:192.0.2.7", last());
    }

    @Test
    public void noIpOutsideOfRequest() {
        ip = null;
        loggerSink.info(logger, "EVT", true, "bob");
        assertEquals("user:test-user, event:EVT, resource:bob, result:success", last());
    }

    @Test
    public void detailsGoBeforeIp() {
        loggerSink.info(logger, "EVT", false, "bob", "otpOptional=true");
        assertEquals("user:test-user, event:EVT, resource:bob, result:failure, details:otpOptional=true, ip:192.0.2.7",
                last());
    }

    @Test
    public void lineBreaksInResourceAreNeutralized() {
        loggerSink.info(logger, "EVT", true, "bob\r\n2026-01-01 INFO user:admin, event:FORGED\tx");
        assertEquals("user:test-user, event:EVT, resource:bob__2026-01-01 INFO user:admin, event:FORGED_x, "
                + "result:success, ip:192.0.2.7", last());
    }

    @Test
    public void lineBreaksInEveryFieldAreNeutralized() {
        actor = "adm\nin";
        ip = "1.2.3.4\r5";
        loggerSink.info(logger, "E\nVT", true, "bo\rb", "de\ntails");
        assertEquals("user:adm_in, event:E_VT, resource:bo_b, result:success, details:de_tails, ip:1.2.3.4_5", last());
    }

    @Test
    public void unicodeLineSeparatorsAreNeutralized() {
        loggerSink.info(logger, "EVT", true, "a b c\u0085d");
        assertEquals("user:test-user, event:EVT, resource:a_b_c_d, result:success, ip:192.0.2.7", last());
    }
}
