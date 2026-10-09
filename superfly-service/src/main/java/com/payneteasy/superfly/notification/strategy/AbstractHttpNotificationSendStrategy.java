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
import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.notification.NotificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base for any send strategy using HTTP to deliver notifications.
 *
 * <p>Transport is the project-wide {@link IHttpClient} abstraction (Apache HttpClient 5 under the
 * hood — see {@code ApacheHC5HttpClient}); the legacy commons-httpclient transport has been removed.
 * Notifications are delivered as {@code application/x-www-form-urlencoded} POST requests, matching
 * the wire format consumer callbacks expect.
 *
 * <p>Every notification is signed (see {@link NotificationSignatures}) with the stored token hash of the
 * subsystem owning the callback URI; a notification that cannot be signed is not sent.
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
    protected SubsystemDao subsystemDao;

    protected void doCall(String uri, String notificationType,
            ParameterSetter parameterSetter) throws NotificationException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("superflyNotification", notificationType);
        parameterSetter.setParameters(params);

        String key = findSigningKey(uri);
        if (key == null) {
            return;
        }
        Map<String, String> signedParams = sign(key, params);

        logger.debug("Sending notification to {} with params {}", uri, params);

        HttpRequest request = HttpRequest.builder()
                .url(uri)
                .method(HttpMethod.POST)
                .headers(new HttpHeaders(List.of(new HttpHeader("Content-Type", CONTENT_TYPE_FORM))))
                .body(encodeForm(signedParams))
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

    /**
     * @return the stored token hash shared by all subsystems with this callback URI, or null (logged) if there is
     * no such subsystem, one of them has no hashed token, or their tokens differ
     */
    private String findSigningKey(String uri) {
        Set<String> keys = new HashSet<>();
        try {
            for (UISubsystemForList subsystem : subsystemDao.getSubsystems()) {
                if (uri != null && uri.equals(subsystem.getCallbackInformation())) {
                    SubsystemAuth auth = subsystemDao.getSubsystemAuth(subsystem.getName());
                    String token = auth == null ? null : auth.getSubsystemToken();
                    if (token == null || !token.startsWith(SubsystemTokenHashes.PREFIX)) {
                        logger.warn("Subsystem {} has no token, not sending a notification to {}",
                                subsystem.getName(), uri);
                        return null;
                    }
                    keys.add(token);
                }
            }
        } catch (RuntimeException e) {
            logger.error("Could not obtain the subsystem token for {}, not sending a notification", uri, e);
            return null;
        }
        if (keys.size() != 1) {
            logger.warn("{} subsystem tokens for callback {}, not sending a notification",
                    keys.isEmpty() ? "No" : "Different", uri);
            return null;
        }
        return keys.iterator().next();
    }

    private Map<String, String> sign(String key, Map<String, String> params) {
        Map<String, String> signedParams = new LinkedHashMap<>(params);
        signedParams.put(NotificationSignatures.TIMESTAMP_PARAMETER, String.valueOf(currentTimeMillis()));
        Map<String, String[]> toSign = new LinkedHashMap<>();
        signedParams.forEach((name, value) -> toSign.put(name, new String[]{value == null ? "" : value}));
        signedParams.put(NotificationSignatures.SIGNATURE_PARAMETER, NotificationSignatures.sign(key, toSign));
        return signedParams;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
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

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    protected interface ParameterSetter {
        void setParameters(Map<String, String> params);
    }
}
