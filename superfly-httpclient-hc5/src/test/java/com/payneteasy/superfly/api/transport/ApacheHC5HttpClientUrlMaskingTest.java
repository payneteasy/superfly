package com.payneteasy.superfly.api.transport;

import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The URL in the error messages and in the log lines of the transport is written without user info, query and
 * fragment.
 *
 * <p>The cases of the masking tests are those of {@code CallbackUrlsTest} (superfly-service), whose rule this
 * module repeats: keep the two in line.
 */
public class ApacheHC5HttpClientUrlMaskingTest {

    private static final String INVALID = "(invalid callback URL)";
    private static final String USER_INFO = "callback-user:dummy-password@";
    private static final String EXTRAS = "?key=dummy-query-secret#dummy-fragment";
    private static final List<String> HIDDEN = List.of("callback-user", "dummy-password", "dummy-query-secret",
            "dummy-fragment");

    private HttpServer server;
    private int serverPort;

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 10);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        serverPort = server.getAddress().getPort();
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void httpAndHttpsUrlsWithoutExtrasAreKept() {
        assertMasked("http://subsystem.example/superfly-callback", "http://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example/superfly-callback", "https://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example", "https://subsystem.example");
        assertMasked("https://subsystem.example/a%20b/c", "https://subsystem.example/a%20b/c");
    }

    @Test
    public void userInfoIsRemoved() {
        assertMasked("https://callback-user:dummy-password@subsystem.example/superfly-callback",
                "https://subsystem.example/superfly-callback");
        assertMasked("https://callback-user@subsystem.example/superfly-callback",
                "https://subsystem.example/superfly-callback");
        assertMasked("http://callback%40user:dummy-password@subsystem.example/cb", "http://subsystem.example/cb");
    }

    @Test
    public void everythingBeforeTheLastAtSignOfTheAuthorityIsUserInfo() {
        assertMasked("http://callback-user@dummy-password@subsystem.example/cb", "http://subsystem.example/cb");
    }

    @Test
    public void atSignInThePathIsNotUserInfo() {
        assertMasked("https://subsystem.example/user@host/cb", "https://subsystem.example/user@host/cb");
        assertMasked("https://callback-user:dummy-password@subsystem.example/user@host/cb",
                "https://subsystem.example/user@host/cb");
    }

    @Test
    public void queryIsRemoved() {
        assertMasked("https://subsystem.example/superfly-callback?key=dummy-query-secret",
                "https://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example?key=dummy-query-secret", "https://subsystem.example");
        assertMasked("https://subsystem.example/superfly-callback?", "https://subsystem.example/superfly-callback");
    }

    @Test
    public void fragmentIsRemoved() {
        assertMasked("https://subsystem.example/superfly-callback#dummy-fragment",
                "https://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example/superfly-callback?key=dummy-query-secret#dummy-fragment",
                "https://subsystem.example/superfly-callback");
    }

    @Test
    public void portIsKept() {
        assertMasked("https://subsystem.example:8443/superfly-callback", "https://subsystem.example:8443/superfly-callback");
        assertMasked("http://callback-user:dummy-password@subsystem.example:8080/cb?key=dummy-query-secret#dummy-fragment",
                "http://subsystem.example:8080/cb");
    }

    @Test
    public void hostWithUnderscoreIsKept() {
        assertMasked("http://superfly_client:8080/cb", "http://superfly_client:8080/cb");
        assertMasked("http://superfly_client/cb", "http://superfly_client/cb");
        assertMasked("http://callback-user:dummy-password@superfly_client:8080/cb?key=dummy-query-secret#dummy-fragment",
                "http://superfly_client:8080/cb");
        assertMasked("https://callback-user@sub_system.example/cb", "https://sub_system.example/cb");
    }

    @Test
    public void ipv6HostIsKept() {
        assertMasked("http://[::1]:8080/cb", "http://[::1]:8080/cb");
        assertMasked("https://[2001:db8::1]/cb", "https://[2001:db8::1]/cb");
        assertMasked("https://callback-user:dummy-password@[2001:db8::1]:8443/cb?key=dummy-query-secret#dummy-fragment",
                "https://[2001:db8::1]:8443/cb");
    }

    @Test
    public void urlThatCannotBeParsedIsNotRepeated() {
        assertMasked("https://callback-user:dummy-password@subsystem.example/superfly callback?key=dummy-query-secret",
                INVALID);
        assertMasked("https://subsystem.example/cb{id}?key=dummy-query-secret#dummy fragment", INVALID);
        assertMasked("superfly_client:8080/cb?key=dummy-query-secret", INVALID);
    }

    @Test
    public void urlThatIsNotAbsoluteWithAHostIsNotRepeated() {
        assertMasked("", INVALID);
        assertMasked("/cb?key=dummy-query-secret", INVALID);
        assertMasked("//subsystem.example/cb?key=dummy-query-secret", INVALID);
        assertMasked("callback-user:dummy-password@subsystem.example/cb", INVALID);
        assertMasked("subsystem.example:8080/cb?key=dummy-query-secret", INVALID);
        assertMasked("mailto:callback-user@subsystem.example", INVALID);
        assertMasked("https:///cb?key=dummy-query-secret", INVALID);
        assertMasked("https://callback-user:dummy-password@/cb", INVALID);
    }

    @Test
    public void hostWithNonAsciiLettersIsKept() {
        assertMasked("http://m\u00fcnchen.example/cb", "http://m\u00fcnchen.example/cb");
        assertMasked("http://callback-user:dummy-password@m\u00fcnchen.example:8080/cb?key=dummy-query-secret",
                "http://m\u00fcnchen.example:8080/cb");
    }

    @Test
    public void atSignInTheQueryAfterThePathIsNotUserInfo() {
        assertMasked("https://subsystem.example/cb?email=user@subsystem.example", "https://subsystem.example/cb");
        assertMasked("https://callback-user:dummy-password@subsystem.example/cb?email=user@subsystem.example",
                "https://subsystem.example/cb");
    }

    /** A '/', '?' or '#' ends the authority, so the rest of the user info is taken for the host, path, query or fragment. */
    @Test
    public void userInfoWithASlashIsNotRepeated() {
        assertMasked("https://svc:pa/ss@cb.example/notify", INVALID);
        assertMasked("https://callback-user:dummy/password@subsystem.example/superfly-callback", INVALID);
    }

    @Test
    public void userInfoWithAHashIsNotRepeated() {
        assertMasked("https://svc:pa#ss@cb.example/n", INVALID);
        assertMasked("https://callback-user:dummy#password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user:1234#password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user#password@subsystem.example/superfly-callback", INVALID);
    }

    @Test
    public void userInfoWithAQuestionMarkIsNotRepeated() {
        assertMasked("https://callback-user:dummy?password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user:1234?password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user?password@subsystem.example/superfly-callback", INVALID);
    }

    /** It cannot be told from user info with a '?' in it. */
    @Test
    public void atSignInTheQueryOfAUrlWithoutPathIsNotRepeated() {
        assertMasked("https://subsystem.example?email=user@subsystem.example", INVALID);
    }

    @Test
    public void missingUrlIsShownAsNull() {
        assertEquals("null", ApacheHC5HttpClient.maskedUrl(null));
    }

    @Test
    public void failedConnectionIsReportedWithTheMaskedUrl() throws Exception {
        int freePort = freePort();

        HttpConnectException e = assertThrows(HttpConnectException.class,
                () -> send("http://localhost:" + freePort + "/superfly-callback" + EXTRAS));

        assertMessage("Connection failed to http://localhost:" + freePort + "/superfly-callback", e);
    }

    @Test
    public void responseTimeoutIsReportedWithTheMaskedUrl() throws Exception {
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        HttpReadException e = assertThrows(HttpReadException.class,
                () -> send("http://localhost:" + serverPort + "/slow" + EXTRAS, 5_000, 150));

        assertMessage("Response timeout from http://localhost:" + serverPort + "/slow", e);
    }

    @Test
    public void ioErrorIsReportedWithTheMaskedUrl() throws Exception {
        try (ServerSocket dropping = new ServerSocket(0)) {
            Thread acceptor = new Thread(() -> {
                try {
                    dropping.accept().close();
                } catch (IOException ignored) {
                    // the client gave up, nothing to drop
                }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            HttpReadException e = assertThrows(HttpReadException.class,
                    () -> send("http://localhost:" + dropping.getLocalPort() + "/dropped" + EXTRAS));

            assertMessage("I/O error communicating with http://localhost:" + dropping.getLocalPort() + "/dropped", e);
        }
    }

    /** The transport does not accept user info in the URL; whatever it makes of it, the message names no user info. */
    @Test
    public void failureOfUrlWithUserInfoIsReportedWithTheMaskedUrl() throws Exception {
        int freePort = freePort();
        String masked = " http://localhost:" + freePort + "/superfly-callback";

        try {
            send("http://" + USER_INFO + "localhost:" + freePort + "/superfly-callback" + EXTRAS);
        } catch (HttpConnectException | HttpReadException e) {
            assertTrue(e.getMessage(), e.getMessage().endsWith(masked));
            assertNothingHidden(e.getMessage());
        }
    }

    /**
     * Relies on 10.255.255.1 being non-routable (SYN dropped): the connect times out. Whatever else the environment
     * makes of the address, the message of a failure names the URL without query and fragment.
     */
    @Test
    public void connectTimeoutIsReportedWithTheMaskedUrl() throws Exception {
        try {
            send("http://10.255.255.1:81/superfly-callback" + EXTRAS, 500, 1_000);
        } catch (HttpConnectException | HttpReadException e) {
            assertTrue(e.getMessage(), e.getMessage().endsWith(" http://10.255.255.1:81/superfly-callback"));
            assertNothingHidden(e.getMessage());
        }
    }

    @Test
    public void logLinesOfAFailedRequestNameTheMaskedUrl() throws Exception {
        int freePort = freePort();
        String masked = "http://localhost:" + freePort + "/superfly-callback";
        RecordingSlf4jProvider.LINES.clear();

        try {
            send("http://" + USER_INFO + "localhost:" + freePort + "/superfly-callback" + EXTRAS);
        } catch (HttpConnectException | HttpReadException e) {
            // the message of the failure is covered by the tests above
        }

        List<String> debug = transportLines("DEBUG");
        assertEquals(debug.toString(), 1, debug.size());
        assertTrue(debug.get(0), debug.get(0).startsWith("send: method=POST url=" + masked + " timeouts="));
        List<String> warn = transportLines("WARN");
        assertEquals(warn.toString(), 1, warn.size());
        assertTrue(warn.get(0), warn.get(0).contains(" url=" + masked + " error="));
        assertNothingHidden(debug.get(0));
        assertNothingHidden(warn.get(0));
    }

    @Test
    public void logLineOfAnAnsweredRequestNamesTheMaskedUrl() throws Exception {
        server.createContext("/answer", exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        RecordingSlf4jProvider.LINES.clear();

        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
            client.send(HttpRequest.builder().url("http://localhost:" + serverPort + "/answer" + EXTRAS)
                            .method(HttpMethod.POST).build(),
                    HttpRequestParameters.builder().timeouts(new HttpTimeouts(1_000, 1_000)).build());
        }

        List<String> debug = transportLines("DEBUG");
        assertEquals(debug.toString(), 2, debug.size());
        assertTrue(debug.get(1), debug.get(1).startsWith("send: status=204 "));
        assertTrue(debug.get(1), debug.get(1).endsWith(" url=http://localhost:" + serverPort + "/answer"));
        assertNothingHidden(debug.get(0));
        assertNothingHidden(debug.get(1));
    }

    /** The lines of the given level the transport writes about a request. */
    private static List<String> transportLines(String level) {
        return RecordingSlf4jProvider.LINES.stream()
                .filter(line -> line.level().equals(level)
                        && line.logger().equals(ApacheHC5HttpClient.class.getName())
                        && line.message().startsWith("send: "))
                .map(RecordingSlf4jProvider.Line::message)
                .collect(Collectors.toList());
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void send(String url) throws Exception {
        send(url, 1_000, 1_000);
    }

    private void send(String url, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
            client.send(HttpRequest.builder().url(url).method(HttpMethod.POST).build(),
                    HttpRequestParameters.builder().timeouts(new HttpTimeouts(connectTimeoutMs, readTimeoutMs)).build());
            fail("expected a failure to send to " + url);
        }
    }

    private static void assertMessage(String expected, Exception e) {
        assertEquals(expected, e.getMessage());
        assertNothingHidden(e.getMessage());
    }

    private static void assertNothingHidden(String text) {
        for (String hidden : HIDDEN) {
            assertFalse(text, text.contains(hidden));
        }
    }

    private static void assertMasked(String url, String expected) {
        assertEquals(url, expected, ApacheHC5HttpClient.maskedUrl(url));
    }
}
