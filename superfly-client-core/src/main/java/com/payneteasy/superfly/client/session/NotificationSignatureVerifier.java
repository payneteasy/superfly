package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.client.utils.CommonUtils;
import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.common.utils.StringUtils;
import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Verifies that a notification comes from the Superfly server: it must carry a valid signature
 * (see {@link NotificationSignatures}) made with the hash of the subsystem token and a fresh timestamp.
 * Until the subsystem token is configured, every notification is rejected.
 */
public class NotificationSignatureVerifier {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSignatureVerifier.class);

    /** Filter init-param with the subsystem token. */
    public static final String TOKEN_INIT_PARAMETER = "notificationSecret";
    /** Property of the filter 'propertiesResource' with the subsystem token. */
    public static final String TOKEN_PROPERTY = "notification.secret";

    private volatile String key;

    /**
     * @param subsystemToken the subsystem token as issued by Superfly (not its hash); null or blank removes it
     */
    public void setSubsystemToken(String subsystemToken) {
        key = StringUtils.hasText(subsystemToken) ? SubsystemTokenHashes.hash(subsystemToken.trim()) : null;
    }

    /**
     * Takes the token from the init-param or, if it is absent, from the {@value #TOKEN_PROPERTY} property of the
     * properties resource. Keeps the current token if neither is set.
     *
     * @param initParameterValue value of the {@value #TOKEN_INIT_PARAMETER} init-param, may be null
     * @param propertiesResource properties location (see {@link CommonUtils#loadPropertiesThrowing(String)}), may be null
     */
    public void configure(String initParameterValue, String propertiesResource) {
        String token = initParameterValue;
        if (!StringUtils.hasText(token) && propertiesResource != null) {
            token = CommonUtils.loadPropertiesThrowing(propertiesResource).getProperty(TOKEN_PROPERTY);
        }
        if (StringUtils.hasText(token)) {
            setSubsystemToken(token);
        }
    }

    public boolean isAuthentic(Map<String, String[]> parameters, String remoteAddr) {
        return isAuthentic(parameters, remoteAddr, System.currentTimeMillis());
    }

    boolean isAuthentic(Map<String, String[]> parameters, String remoteAddr, long nowMillis) {
        String currentKey = key;
        if (currentKey == null) {
            logger.warn("Rejected Superfly notification from {}: the subsystem token to verify it is not configured ('{}')",
                    remoteAddr, TOKEN_INIT_PARAMETER);
            return false;
        }
        NotificationSignatures.Result result = NotificationSignatures.verify(currentKey, parameters, nowMillis,
                NotificationSignatures.DEFAULT_MAX_CLOCK_SKEW_MILLIS);
        if (result != NotificationSignatures.Result.VALID) {
            logger.warn("Rejected Superfly notification from {}: {}", remoteAddr, result);
            return false;
        }
        return true;
    }
}
