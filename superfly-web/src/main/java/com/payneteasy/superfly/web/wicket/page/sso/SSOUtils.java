package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;

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
        clearLoginData((SessionAccessorPage) component.getPage());
        redirectToLoginErrorPage(component, new Model<String>("Can't login to " + loginData.getSubsystemIdentifier()));
    }

    public static void redirectToLoginErrorPage(Component component, IModel<String> errorModel) {
        component.getRequestCycle().setResponsePage(
                new SSOLoginErrorPage(errorModel));
        component.getRequestCycle().setRedirect(true);
    }

    public static void saveLoginData(SessionAccessorPage page, SSOLoginData loginData) {
        page.getSession().setSsoLoginData(loginData);
    }

    public static void clearLoginData(SessionAccessorPage page) {
        saveLoginData(page, null);
    }

    public static void redirectToSubsystem(SessionAccessorPage page, SSOLoginData loginData, SubsystemTokenData token) {
        SSOUtils.clearLoginData(page);
        RedirectRequestTarget requestTarget = new RedirectRequestTarget(
                buildRedirectToSubsystemUrl(token.getLandingUrl(),
                        token.getSubsystemToken(), loginData.getTargetUrl())
        );
        page.getRequestCycle().setRequestTarget(requestTarget);
    }
}
