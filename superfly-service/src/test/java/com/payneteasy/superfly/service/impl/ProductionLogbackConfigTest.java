package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The production log pattern must not let user input forge a record (log injection).
 */
public class ProductionLogbackConfigTest {

    @Test
    public void lineBreaksInMessageDoNotSplitTheRecord() throws Exception {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        // surefire runs with the module directory as the working directory
        configurator.doConfigure(new File("../docker/jetty/logback.xml"));
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        ConsoleAppender<ILoggingEvent> console = (ConsoleAppender<ILoggingEvent>) root.getAppender("CONSOLE");
        // ConsoleAppender always writes to System.out: reuse its encoder with an in-memory stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamAppender<ILoggingEvent> capture = new OutputStreamAppender<>();
        capture.setContext(context);
        capture.setEncoder(console.getEncoder());
        capture.setOutputStream(out);
        capture.start();
        root.detachAppender(console);
        root.addAppender(capture);

        context.getLogger("test").warn("Login failed. No session for user <{}>", "bob\r\n2026-01-01 INFO forged\tx");

        String output = out.toString(StandardCharsets.UTF_8);
        String[] lines = output.split("\\R");
        assertEquals(output, 1, lines.length);
        assertTrue(lines[0], lines[0].endsWith("Login failed. No session for user <bob_2026-01-01 INFO forged_x>"));
        context.stop();
    }
}
