package com.payneteasy.superfly.web.security.securehandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SubsystemService;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Base64;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SecuredHttpRequestHandlerLogTest {

    @Test
    public void authorizationHeaderAndTokenAreNotLogged() throws Exception {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setSubsystemToken("tok-s3cret");
        SubsystemService subsystems = createMock(SubsystemService.class);
        expect(subsystems.getSubsystemByName("billing")).andReturn(subsystem);
        replay(subsystems);
        SecuredHttpRequestHandler handler = new SecuredHttpRequestHandler(
                (request, response) -> { }, new BasicAuthorizationParser(), subsystems);

        Logger logger = (Logger) LoggerFactory.getLogger(SecuredHttpRequestHandler.class);
        Level oldLevel = logger.getLevel();
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(logs);
        try {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/x");
            String encoded = Base64.getEncoder().encodeToString("billing:tok-s3cret".getBytes());
            request.addHeader("Authorization", "Basic " + encoded);
            handler.handleRequest(request, new MockHttpServletResponse());
        } finally {
            logger.detachAppender(logs);
            logger.setLevel(oldLevel);
        }

        assertTrue(logs.list.stream().anyMatch(e -> e.getFormattedMessage().contains("billing")));
        for (ILoggingEvent event : logs.list) {
            String message = event.getFormattedMessage();
            assertFalse(message, message.contains("tok-s3cret"));
            assertFalse(message, message.contains("Basic"));
        }
    }
}
