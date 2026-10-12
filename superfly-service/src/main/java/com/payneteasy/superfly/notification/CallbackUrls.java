package com.payneteasy.superfly.notification;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Subsystem callback URL in the form that may be written to logs and exception messages.
 */
public final class CallbackUrls {

    private static final String INVALID = "(invalid callback URL)";

    private CallbackUrls() {
    }

    /**
     * The callback URL without user info, query and fragment: they may carry credentials. The scheme, the host,
     * the port and the path are kept.
     *
     * <p>The same rule is applied to the URL in the error messages of {@code ApacheHC5HttpClient}
     * (superfly-httpclient-hc5), which cannot use this class: keep the two in line.
     *
     * @param url    callback URL as stored for the subsystem, may be null
     * @return the URL to write to a log; {@code "(invalid callback URL)"} if it cannot be parsed or is not an absolute
     * URL with a host, since repeating such a URL would repeat the parts to hide
     */
    public static String masked(String url) {
        if (url == null) {
            return "null";
        }
        URI parsed;
        try {
            parsed = new URI(url);
        } catch (URISyntaxException e) {
            return INVALID;
        }
        String authority = parsed.getRawAuthority();
        if (parsed.getScheme() == null || authority == null) {
            return INVALID;
        }
        // User info is everything before the last '@': URI#getUserInfo() is null (and URI#getHost() too) for a
        // registry-based authority, which is what a host name with '_' (a docker-compose service name) parses to
        String hostAndPort = authority.substring(authority.lastIndexOf('@') + 1);
        if (hostAndPort.isEmpty()) {
            return INVALID;
        }
        return parsed.getScheme() + "://" + hostAndPort + parsed.getRawPath();
    }
}
