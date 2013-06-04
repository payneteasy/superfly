package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author rpuch
 */
public class SSOUtils {
    private static final Logger logger = LoggerFactory.getLogger(SSOUtils.class);

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
        anonymizeLoginData((SessionAccessorPage) component.getPage());
        redirectToLoginErrorPage(component, new Model<String>("Can't login to " + loginData.getSubsystemIdentifier()));
    }

    public static void redirectToLoginErrorPage(Component component, IModel<String> errorModel) {
        component.getRequestCycle().setResponsePage(
                new SSOLoginErrorPage(errorModel));
    }

    public static void saveLoginData(SessionAccessorPage page, SSOLoginData loginData) {
        page.getSession().setSsoLoginData(loginData);
    }

    public static void anonymizeLoginData(SessionAccessorPage page) {
        SSOLoginData loginData = getSsoLoginData(page);
        if (loginData != null) {
            loginData.setUsername(null);
        }
    }

    public static SSOLoginData getSsoLoginData(SessionAccessorPage page) {
        return page.getSession().getSsoLoginData();
    }

    public static void redirectToSubsystem(SessionAccessorPage page, SSOLoginData loginData, SubsystemTokenData token) {
        if (loginData == null) {
            // possibly, session has expired
            logger.error("Tried to redirect to subsystem with loginData=null", new RuntimeException());
            redirectToLoginErrorPage(page, new Model<String>("Session has expired. Please try to login again."));
        } else {
            SSOUtils.anonymizeLoginData(page);
            String url = buildRedirectToSubsystemUrl(token.getLandingUrl(),
                    token.getSubsystemToken(), loginData.getTargetUrl());
            redirect(page, url);
        }
    }

    public static void redirect(Page page, String url) {
        IRequestHandler requestTarget = new RedirectRequestHandler(url);
        page.getRequestCycle().replaceAllRequestHandlers(requestTarget);
    }

    public static String getSsoSessionIdFromCookie(WebRequest request) {
        String ssoSessionId = null;
        Cookie cookie = request.getCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME);
        if (cookie != null) {
            ssoSessionId = cookie.getValue();
        }
        return ssoSessionId;
    }

    public static void onSuccessfulLogin(String username,
            SessionAccessorPage page,
            SSOLoginData loginData, SessionService sessionService,
            SubsystemService subsystemService) {
        if (loginData == null) {
            throw new IllegalStateException("loginData is null");
        }

        SSOSession ssoSession = sessionService.createSSOSession(username);
        Cookie cookie = new Cookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, ssoSession.getIdentifier());
        cookie.setMaxAge(SSOUtils.SSO_SESSION_ID_COOKIE_MAXAGE);
        cookie.setPath(getApplicationRootPath(page));
        ((WebResponse) RequestCycle.get().getResponse()).addCookie(cookie);

        SubsystemTokenData token = subsystemService.issueSubsystemTokenIfCanLogin(
                ssoSession.getId(), loginData.getSubsystemIdentifier());
        if (token != null) {
            // can login: redirecting a user to a subsystem
            SSOUtils.redirectToSubsystem(page, loginData, token);
        } else {
            // can't login: just display an error
            // actually, this should not happen as we've already
            // checked user access, but just in case...
            SSOUtils.redirectToCantLoginErrorPage(page, loginData);
        }
    }

    private static String getApplicationRootPath(SessionAccessorPage page) {
        String contextPath = ((ServletWebRequest) page.getRequest()).getContextPath();
        String filterPath = ((ServletWebRequest) page.getRequest()).getFilterPath();
        return contextPath + filterPath;
    }

}
