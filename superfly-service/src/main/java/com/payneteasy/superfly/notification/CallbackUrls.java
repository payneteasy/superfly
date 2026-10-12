package com.payneteasy.superfly.notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Subsystem callback URL in the form that may be written to logs and exception messages.
 */
public final class CallbackUrls {

    private static final String INVALID = "(invalid callback URL)";

    /** The host and optional port of a registry-based authority, which URI#getHost() does not recognise. */
    private static final Pattern HOST_AND_PORT = Pattern.compile("[\\p{L}\\p{N}._~-]+(?::[0-9]*)?");

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
     * @return the URL to write to a log; {@code "(invalid callback URL)"} if it cannot be parsed, is not an absolute
     * URL with a host, or has user info that cannot be told apart from the rest of the URL (it has a '/', '?' or '#'):
     * repeating such a URL would repeat the parts to hide
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
        // A '/', '?' or '#' in the user info ends the authority there, and the rest of the user info lands in the host
        // (a registry-based authority when the port is not a number), the path, the query or the fragment: it cannot
        // be told from them and so cannot be removed. An IPv6 literal is never a registry-based authority
        if (parsed.getHost() == null && !HOST_AND_PORT.matcher(hostAndPort).matches()) {
            return INVALID;
        }
        String authorityPart = parsed.getScheme() + "://" + authority;
        int at = url.indexOf('@', authorityPart.length());
        int slash = url.indexOf('/', authorityPart.length());
        if (!url.startsWith(authorityPart) || (at >= 0 && (slash < 0 || at < slash))) {
            return INVALID;
        }
        return parsed.getScheme() + "://" + hostAndPort + parsed.getRawPath();
    }
}
