package com.payneteasy.superfly.web.wicket;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener.CsrfAction;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.util.string.Strings;

/**
 * CSRF protection for Wicket listener invocations (links, forms, ajax): accepted only when the browser
 * reports the request as initiated by our own pages ({@code Sec-Fetch-Site: same-origin}) or by the user
 * ({@code none}).
 *
 * <p>Stricter than Wicket's {@code FetchMetadataResourceIsolationPolicy}, which lets cross-site top-level
 * GET navigations through — exactly how an admin {@code Link} (lock user, reset password) would be forged.
 * Page renders are not checked by the listener, so SSO redirects from subsystems are unaffected.
 *
 * <p>Browsers without Fetch Metadata fall back to Origin, then Referer: a foreign host is rejected. Only the
 * host is compared — scheme and port differ from what the browser sees behind a TLS-terminating proxy. With
 * none of the three headers the request is let through, as before, so clients that strip headers keep working.
 */
public class SameOriginResourceIsolationPolicy implements IResourceIsolationPolicy {

    private static final String SEC_FETCH_SITE = "Sec-Fetch-Site";

    public static ResourceIsolationRequestCycleListener newRequestCycleListener() {
        return new ResourceIsolationRequestCycleListener(new SameOriginResourceIsolationPolicy())
                .setUnknownOutcomeAction(CsrfAction.ALLOW)
                .setDisallowedOutcomeAction(CsrfAction.ABORT);
    }

    @Override
    public ResourceIsolationOutcome isRequestAllowed(HttpServletRequest request, IRequestablePage targetPage) {
        String site = request.getHeader(SEC_FETCH_SITE);
        if (Strings.isEmpty(site)) {
            return fallbackOutcome(request);
        }
        return "same-origin".equals(site) || "none".equals(site)
                ? ResourceIsolationOutcome.ALLOWED
                : ResourceIsolationOutcome.DISALLOWED;
    }

    private static ResourceIsolationOutcome fallbackOutcome(HttpServletRequest request) {
        String source = request.getHeader("Origin");
        if (Strings.isEmpty(source)) {
            source = request.getHeader("Referer");
        }
        if (Strings.isEmpty(source)) {
            return ResourceIsolationOutcome.UNKNOWN;
        }
        String host = hostOf(source);
        return host != null && host.equalsIgnoreCase(request.getServerName())
                ? ResourceIsolationOutcome.ALLOWED
                : ResourceIsolationOutcome.DISALLOWED;
    }

    private static String hostOf(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void setHeaders(HttpServletResponse response) {
        response.addHeader("Vary", SEC_FETCH_SITE);
    }
}
