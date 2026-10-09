package com.payneteasy.superfly.web.spring.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.payneteasy.superfly.common.utils.LogSanitizer.forLog;

/**
 * Builds the Content-Security-Policy value: the base policy plus subsystem origins in form-action and style-src.
 * Only a strictly validated {@code scheme://host[:port]} ever reaches the header, so a hostile URL
 * cannot inject extra directives.
 */
final class CspPolicyBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CspPolicyBuilder.class);

    private static final Pattern HOST = Pattern.compile("[A-Za-z0-9]([A-Za-z0-9.-]*[A-Za-z0-9])?|\\[[0-9A-Fa-f:.]+]");

    private CspPolicyBuilder() {
    }

    static String build(Collection<String> formActionUrls, Collection<String> styleUrls) {
        return "default-src 'self'; " +
               "script-src 'self' 'unsafe-inline'; " +
               "style-src 'self' 'unsafe-inline'" + origins(styleUrls, true) + "; " +
               "img-src 'self' data:; " +
               "frame-ancestors 'none'; " +
               "form-action 'self'" + origins(formActionUrls, false);
    }

    // A stylesheet over plain http can be swapped by a MITM, and hostile CSS can exfiltrate the CSRF token.
    private static String origins(Collection<String> urls, boolean httpsOnly) {
        Set<String> origins = new TreeSet<>();
        for (String url : urls) {
            String origin = toOrigin(url);
            if (origin != null && httpsOnly && !origin.startsWith("https://")) {
                skip(url, "only https is allowed for style-src");
            } else if (origin != null) {
                origins.add(origin);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String origin : origins) {
            sb.append(' ').append(origin);
        }
        return sb.toString();
    }

    static String toOrigin(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            int port = uri.getPort();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return skip(url, "scheme is not http/https");
            }
            if (uri.getUserInfo() != null) {
                return skip(url, "userinfo is not allowed");
            }
            if (host == null || !HOST.matcher(host).matches()) {
                return skip(url, "invalid host");
            }
            if (port != -1 && (port < 1 || port > 65535)) {
                return skip(url, "invalid port");
            }
            return scheme + "://" + host.toLowerCase(Locale.ROOT) + (port == -1 ? "" : ":" + port);
        } catch (URISyntaxException e) {
            return skip(url, "malformed URL");
        }
    }

    private static String skip(String url, String reason) {
        logger.warn("Subsystem URL is not added to CSP ({}): {}", reason, forLog(url, 200));
        return null;
    }
}
