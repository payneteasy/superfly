package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.web.wicket.page.login.LoginErrorPage;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author rpuch
 */
public class SSOUtils {
    public static final String SSO_SESSION_ID_COOKIE_NAME = "SSOSESSIONID";
    public static final int SSO_SESSION_ID_COOKIE_MAXAGE = 3600; // seconds

    public static String buildRedirectToSubsystemUrl(String landingUrl, String subsystemToken, String targetUrl) {
        StringBuilder buf = new StringBuilder();
        buf.append(landingUrl);
        buf.append(landingUrl.contains("?") ? "&" : "?");
        buf.append("subsystemToken").append("=").append(encodeForUrl(subsystemToken));
        buf.append("&");
        buf.append("targetUrl").append("=").append(encodeForUrl(targetUrl));
        return buf.toString();
    }

    private static String encodeForUrl(String subsystemToken) {
        try {
            return URLEncoder.encode(subsystemToken, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void redirectToCantLoginErrorPage(Component component, SSOLoginData loginData) {
        component.getRequestCycle().setResponsePage(
                new LoginErrorPage(new Model<String>("Can't login to " + loginData.getSubsystemIdentifier())));
        component.getRequestCycle().setRedirect(true);
    }
}
