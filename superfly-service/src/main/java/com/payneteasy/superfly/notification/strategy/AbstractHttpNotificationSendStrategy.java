package com.payneteasy.superfly.notification.strategy;

import com.payneteasy.http.client.api.HttpHeader;
import com.payneteasy.http.client.api.HttpHeaders;
import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import com.payneteasy.superfly.notification.NotificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base for any send strategy using HTTP to deliver notifications.
 *
 * <p>Transport is the project-wide {@link IHttpClient} abstraction (Apache HttpClient 5 under the
 * hood — see {@code ApacheHC5HttpClient}); the legacy commons-httpclient transport has been removed.
 * Notifications are delivered as {@code application/x-www-form-urlencoded} POST requests, matching
 * the wire format consumer callbacks expect.
 *
 * @author Roman Puchkovskiy
 */
public abstract class AbstractHttpNotificationSendStrategy implements
        NotificationSendStrategy {

    /** Connect timeout for notification callbacks (ms). Mirrors the former commons-httpclient setup. */
    private static final int    CONNECT_TIMEOUT_MS = 10_000;

    /** Read/response timeout for notification callbacks (ms). Mirrors the former commons-httpclient setup. */
    private static final int    READ_TIMEOUT_MS    = 30_000;

    private static final String CONTENT_TYPE_FORM  = "application/x-www-form-urlencoded";

    protected static Logger logger = LoggerFactory.getLogger(AbstractHttpNotificationSendStrategy.class);

    protected IHttpClient httpClient;

    protected void doCall(String uri, String notificationType,
            ParameterSetter parameterSetter) throws NotificationException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("superflyNotification", notificationType);
        parameterSetter.setParameters(params);

        logger.debug("Sending notification to {} with params {}", uri, params);

        HttpRequest request = HttpRequest.builder()
                .url(uri)
                .method(HttpMethod.POST)
                .headers(new HttpHeaders(List.of(new HttpHeader("Content-Type", CONTENT_TYPE_FORM))))
                .body(encodeForm(params))
                .build();
        HttpRequestParameters parameters = HttpRequestParameters.builder()
                .timeouts(new HttpTimeouts(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS))
                .build();

        try {
            HttpResponse response = httpClient.send(request, parameters);
            if (logger.isInfoEnabled()) {
                logger.info("Successfully notified {} with params {} (status {})",
                        uri, params, response.getStatusCode());
            }
        } catch (HttpConnectException | HttpReadException | HttpWriteException e) {
            throw new NotificationException(e);
        }
    }

    private static byte[] encodeForm(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
              .append('=')
              .append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Autowired
    public void setHttpClient(IHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected interface ParameterSetter {
        void setParameters(Map<String, String> params);
    }
}
