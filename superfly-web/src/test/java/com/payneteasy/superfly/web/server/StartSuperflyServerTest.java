package com.payneteasy.superfly.web.server;

import com.payneteasy.startup.parameters.StartupParametersBuilder;
import com.payneteasy.superfly.web.IStartSuperflyConfig;
import com.payneteasy.superfly.web.SuperflyServer;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StartSuperflyServerTest {

    private Server server;

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    private static IStartSuperflyConfig config(Map<String, String> values) {
        Map<String, String> all = new HashMap<>(values);
        return new StartupParametersBuilder()
                .addLoader("t", all::get)
                .getStartupParameters(IStartSuperflyConfig.class);
    }

    private HttpResponse<String> get(Server started, Map<String, String> headers) throws Exception {
        int port = ((ServerConnector) started.getConnectors()[0]).getLocalPort();
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"));
        headers.forEach(request::header);
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Server startServer(Map<String, String> values) throws Exception {
        Map<String, String> all = new HashMap<>(values);
        all.put("JETTY_PORT", "0");
        all.put("DB_HOST", "db.example.invalid");
        all.put("DB_PORT", "3307");
        all.put("DB_NAME", "ssodb");
        all.put("DB_USER", "fake-user");
        all.put("DB_PASSWORD", "fake-password");
        server = new SuperflyServer(getClass().getResource("/embedded-webapp/"))
                .createServer(config(all));
        server.start();
        return server;
    }

    @Test
    public void answersAndResolvesJndiDatasourceFromDbSettings() throws Exception {
        HttpResponse<String> response = get(startServer(Map.of()), Map.of());

        assertEquals(200, response.statusCode());
        assertTrue(response.body(), response.body().contains("ds-url=jdbc:mysql://db.example.invalid:3307/ssodb?"));
        assertTrue(response.body(), response.body().contains("serverTimezone=UTC"));
        assertTrue(response.body(), response.body().contains("ds-user=fake-user"));
        assertFalse("password must not leak", response.body().contains("fake-password"));
    }

    @Test
    public void ignoresForwardedHeadersByDefault() throws Exception {
        HttpResponse<String> response = get(startServer(Map.of()), Map.of("X-Forwarded-Proto", "https"));

        assertTrue(response.body(), response.body().contains("scheme=http\n"));
    }

    @Test
    public void honoursForwardedHeadersOnlyWhenTrusted() throws Exception {
        HttpResponse<String> response = get(startServer(Map.of("JETTY_TRUST_FORWARDED", "true")),
                Map.of("X-Forwarded-Proto", "https"));

        assertTrue(response.body(), response.body().contains("scheme=https\n"));
    }

    @Test
    public void forwardedCustomizerIsAddedOnlyByFlag() {
        SuperflyServer superflyServer = new SuperflyServer(null);

        assertTrue(hasForwardedCustomizer(superflyServer, Map.of("JETTY_TRUST_FORWARDED", "true")));
        assertFalse(hasForwardedCustomizer(superflyServer, Map.of()));
    }

    private static boolean hasForwardedCustomizer(SuperflyServer superflyServer, Map<String, String> values) {
        return superflyServer.createHttpConfiguration(config(values)).getCustomizers().stream()
                .anyMatch(ForwardedRequestCustomizer.class::isInstance);
    }

    @Test(expected = IllegalStateException.class)
    public void sslPortWithoutKeystoreIsRejected() throws Exception {
        new SuperflyServer(getClass().getResource("/embedded-webapp/"))
                .createServer(config(Map.of("JETTY_PORT_SSL", "0")));
    }
}
