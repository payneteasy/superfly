package com.payneteasy.superfly.api.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SSOHttpServiceApiClientSecurityTest {

    private static final String SUBSYSTEM_NAME  = "test-subsystem";
    private static final String SUBSYSTEM_TOKEN = "test-token";
    private static final String HTTPS_URL       = "https://test.example.com/superfly";

    private IHttpClient          mockHttpClient;
    private ApiSerializationManager serializer;
    private ListAppender<ILoggingEvent> logCapture;
    private Logger               clientLogger;
    private Level                originalLevel;

    @Before
    public void setUp() {
        mockHttpClient = createMock(IHttpClient.class);
        serializer = new ApiSerializationManager();

        clientLogger = (Logger) LoggerFactory.getLogger(SSOHttpServiceApiClient.class);
        originalLevel = clientLogger.getLevel();
        clientLogger.setLevel(Level.DEBUG);

        logCapture = new ListAppender<>();
        logCapture.start();
        clientLogger.addAppender(logCapture);
    }

    @After
    public void tearDown() {
        clientLogger.detachAppender(logCapture);
        clientLogger.setLevel(originalLevel);
        System.clearProperty("superfly.client.allowInsecureScheme");
    }

    // ── HTTPS enforcement ────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void testHttpUrlRejected() {
        newClient("http://insecure.example.com/sso");
    }

    @Test
    public void testHttpsUrlAccepted() {
        SSOHttpServiceApiClient client = newClient(HTTPS_URL);
        assertNotNull(client);
    }

    @Test
    public void testHttpUrlAllowedViaSystemProperty() {
        System.setProperty("superfly.client.allowInsecureScheme", "true");
        SSOHttpServiceApiClient client = newClient("http://allowed.example.com/sso");
        assertNotNull(client);
    }

    @Test
    public void testInsecureBypassLogsWarn() {
        System.setProperty("superfly.client.allowInsecureScheme", "true");
        newClient("http://allowed.example.com/sso");

        boolean warnFound = logCapture.list.stream()
                .anyMatch(e -> e.getLevel() == Level.WARN && e.getFormattedMessage().contains("Insecure"));
        assertTrue("WARN must be logged when http:// bypass is used", warnFound);
    }

    // ── Credential leak prevention ───────────────────────────────────────────

    @Test
    public void testDebugLogsDoNotContainPassword() throws Exception {
        expect(mockHttpClient.send(anyObject(), anyObject()))
                .andThrow(new HttpConnectException("refused", new Exception()));
        replay(mockHttpClient);

        SSOHttpServiceApiClient client = newTestableClient();

        try {
            client.authenticate(new AuthenticateRequest("alice", "secret-p@ssword-123"));
        } catch (Exception e) {
            // expected — no server
        }

        for (ILoggingEvent event : logCapture.list) {
            String msg = event.getFormattedMessage();
            assertFalse("DEBUG log must not contain password. Got: " + msg,
                    msg.contains("secret-p@ssword-123"));
        }

        verify(mockHttpClient);
    }

    @Test
    public void testDebugLogsDoNotContainSubsystemToken() throws Exception {
        expect(mockHttpClient.send(anyObject(), anyObject()))
                .andThrow(new HttpConnectException("refused", new Exception()));
        replay(mockHttpClient);

        SSOHttpServiceApiClient client = newTestableClient();

        try {
            client.authenticate(new AuthenticateRequest("alice", "pass"));
        } catch (Exception e) {
            // expected
        }

        for (ILoggingEvent event : logCapture.list) {
            String msg = event.getFormattedMessage();
            assertFalse("DEBUG log must not contain subsystem token. Got: " + msg,
                    msg.contains(SUBSYSTEM_TOKEN));
        }

        verify(mockHttpClient);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private SSOHttpServiceApiClient newClient(String baseUrl) {
        return new SSOHttpServiceApiClient(
                HttpRequestParameters.builder().build(),
                baseUrl,
                SUBSYSTEM_NAME,
                SUBSYSTEM_TOKEN,
                serializer
        );
    }

    private SSOHttpServiceApiClient newTestableClient() {
        return new SSOHttpServiceApiClient(
                HttpRequestParameters.builder().build(),
                HTTPS_URL,
                SUBSYSTEM_NAME,
                SUBSYSTEM_TOKEN,
                serializer
        ) {
            @Override
            protected IHttpClient getHttpClient() {
                return mockHttpClient;
            }
        };
    }
}
