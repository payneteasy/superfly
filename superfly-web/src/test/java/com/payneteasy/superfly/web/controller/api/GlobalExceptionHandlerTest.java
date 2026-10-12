package com.payneteasy.superfly.web.controller.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.eclipse.jetty.io.EofException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GlobalExceptionHandlerTest {

    private ListAppender<ILoggingEvent> logs;
    private Logger handlerLogger;
    private Level oldLevel;

    @Before
    public void captureLog() {
        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        oldLevel = handlerLogger.getLevel();
        handlerLogger.setLevel(Level.DEBUG);
        logs = new ListAppender<>();
        logs.start();
        handlerLogger.addAppender(logs);
    }

    @After
    public void releaseLog() {
        handlerLogger.detachAppender(logs);
        handlerLogger.setLevel(oldLevel);
    }

    @Test
    public void exceptionMessageIsNotReturnedToClient() {
        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler()
                .handleException(new IllegalStateException("could not execute procedure ss_secret_proc"));

        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().toString().contains("ss_secret_proc"));
        assertNotNull(response.getBody().get("errorId"));
    }

    @Test
    public void exceptionWithoutMessageIsHandled() {
        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler().handleException(new NullPointerException());

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody().get("errorId"));
    }

    @Test
    public void serverErrorIsLoggedAsErrorWithStackTrace() {
        IllegalStateException failure = new IllegalStateException("could not execute procedure");

        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler().handleException(failure);

        assertEquals(1, logs.list.size());
        ILoggingEvent event = logs.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        assertTrue(event.getFormattedMessage(), event.getFormattedMessage().contains(response.getBody().get("errorId")));
        assertNotNull("the stack trace must be logged", event.getThrowableProxy());
        assertEquals(IllegalStateException.class.getName(), event.getThrowableProxy().getClassName());
    }

    @Test
    public void closedConnectionIsNotAnErrorAndNothingIsWritten() {
        List<Exception> closedByClient = List.of(
                new AsyncRequestNotUsableException("ServletOutputStream failed to write: java.io.IOException: Broken pipe",
                        new IOException("Broken pipe")),
                new IOException("Broken pipe"),
                new IOException("Connection reset by peer"),
                new EofException("Closed"));

        for (Exception e : closedByClient) {
            assertNull("no answer is written to a closed connection: " + e,
                    new GlobalExceptionHandler().handleException(e));
        }

        assertEquals(closedByClient.size(), logs.list.size());
        for (ILoggingEvent event : logs.list) {
            assertEquals(Level.DEBUG, event.getLevel());
            assertNull("no stack trace: " + event.getFormattedMessage(), event.getThrowableProxy());
        }
    }

    @Test
    public void closedConnectionIsOneDebugLineWithTheReason() {
        new GlobalExceptionHandler().handleException(new AsyncRequestNotUsableException(
                "ServletOutputStream failed to write: java.io.IOException: Broken pipe", new IOException("Broken pipe")));

        assertEquals(1, logs.list.size());
        ILoggingEvent event = logs.list.get(0);
        assertEquals(Level.DEBUG, event.getLevel());
        assertTrue(event.getFormattedMessage(), event.getFormattedMessage().contains("Broken pipe"));
        assertNull(event.getThrowableProxy());
    }

    @Test
    public void otherIoFailuresAreStillServerErrors() {
        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler()
                .handleException(new IOException("No space left on device"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals(Level.ERROR, logs.list.get(0).getLevel());
    }

    @Test
    public void failureThatOnlyWrapsABrokenPipeIsStillAServerError() {
        // e.g. a lost database connection: it is not the client that has gone away
        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler()
                .handleException(new IllegalStateException("could not execute procedure", new IOException("Broken pipe")));

        assertEquals(500, response.getStatusCode().value());
        assertEquals(Level.ERROR, logs.list.get(0).getLevel());
    }

    @Test
    public void closedConnectionGetsNoResponseThroughSpringMvc() throws Exception {
        MockHttpServletResponse response = mvc().perform(get("/closed")).andReturn().getResponse();

        assertFalse("nothing must be written to a closed connection", response.isCommitted());
        assertEquals("", response.getContentAsString());
        assertTrue(logs.list.stream().noneMatch(event -> event.getLevel().isGreaterOrEqual(Level.WARN)));
    }

    @Test
    public void otherFailuresStillGetTheJsonErrorThroughSpringMvc() throws Exception {
        MockHttpServletResponse response = mvc().perform(get("/failing").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        assertEquals(500, response.getStatus());
        assertTrue(response.getContentType(), response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("\"errorId\""));
    }

    private static MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class FailingController {
        @GetMapping("/closed")
        String closed() throws IOException {
            throw new AsyncRequestNotUsableException("ServletOutputStream failed to write: java.io.IOException: Broken pipe",
                    new IOException("Broken pipe"));
        }

        @GetMapping("/failing")
        String failing() {
            throw new IllegalStateException("could not execute procedure");
        }
    }
}
