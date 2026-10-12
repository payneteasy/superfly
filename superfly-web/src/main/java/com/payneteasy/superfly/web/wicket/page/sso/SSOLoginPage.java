package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * @author rpuch
 */
public class SSOLoginPage extends BaseSSOPage {
    private static final Logger logger = LoggerFactory.getLogger(SSOLoginPage.class);
    private static final Pattern STATE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{16,128}$");

    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SessionService sessionService;

    public SSOLoginPage() {
        WebRequest request = (WebRequest) getRequest();
        String subsystemIdentifier = request.getRequestParameters().getParameterValue("subsystemIdentifier").toString();
        String targetUrl = request.getRequestParameters().getParameterValue("targetUrl").toString();
        boolean ok = true;
        if (!StringUtils.hasText(subsystemIdentifier)) {
            SSOUtils.redirectToLoginErrorPage(this, new Model<String>("No subsystemIdentifier parameter specified"));
            ok = false;
        }
        if (!StringUtils.hasText(targetUrl)) {
            SSOUtils.redirectToLoginErrorPage(this, new Model<String>("No targetUrl parameter specified"));
            ok = false;
        }

        if (ok) {
            targetUrl = ensureSameOriginPath(sanitizeTargetUrl(targetUrl));

            SSOLoginData loginData = new SSOLoginData(subsystemIdentifier, targetUrl);
            loginData.setState(validState(request.getRequestParameters().getParameterValue("state").toString()));
            SSOUtils.saveLoginData(this, loginData);

            String ssoSessionId = SSOUtils.getSsoSessionIdFromCookie(request);
            boolean needToLogin = true;
            if (StringUtils.hasText(ssoSessionId)) {
                SSOSession ssoSession = sessionService.getValidSSOSession(ssoSessionId);
                if (ssoSession != null) {
                    // session is valid
                    SubsystemTokenData token = subsystemService.issueSubsystemTokenIfCanLogin(ssoSession.getId(),
                            subsystemIdentifier);
                    if (token != null) {
                        // can login: redirecting a user to a subsystem
                        SSOUtils.redirectToSubsystem(this, loginData, token);
                    } else {
                        // can't login: just display an error
                        String reason = String.format("No subsystem token for subsystemIdentifier = '%s', ssoSession id = %s"
                                , subsystemIdentifier, ssoSession.getId());
                        SSOUtils.redirectToCantLoginErrorPage(this, loginData, reason);
                    }
                    needToLogin = false;
                }
            }

            if (needToLogin) {
                UISubsystem subsystem = subsystemService.getSubsystemByName(subsystemIdentifier);
                if (subsystem == null) {
                    logger.warn("Login attempt for unknown subsystem '{}'", subsystemIdentifier);
                    SSOUtils.redirectToLoginErrorPage(this, new Model<String>("Can't login"));
                    return;
                }
                loginData.setSubsystemTitle(subsystem.getTitle());
                loginData.setSubsystemUrl(subsystem.getSubsystemUrl());
                getRequestCycle().setResponsePage(new SSOLoginPasswordPage());
            }
        }
    }

    /** A state of an unexpected shape is dropped, never echoed back. */
    private static String validState(String state) {
        if (state == null) {
            return null;
        }
        if (!STATE_PATTERN.matcher(state).matches()) {
            logger.warn("Ignoring a malformed state parameter");
            return null;
        }
        return state;
    }

    private String sanitizeTargetUrl(String targetUrl) {
        if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
            // there is protocol, strip it off
            int slashIndex = targetUrl.indexOf("/", targetUrl.indexOf("://") + 3);
            if (slashIndex >= 0) {
                // there is some slash after the protocol: this is the start of the url
                // that will remain
                return targetUrl.substring(slashIndex);
            } else {
                // No slash after protocol, so probably we only have hostname.
                // So it's root!
                return "/";
            }
        } else {
            // no protocol, so just returning url ensuring it is absolute
            return targetUrl.startsWith("/") ? targetUrl : "/" + targetUrl;
        }
    }

    /**
     * Keeps only a same-origin path: browsers strip tabs/newlines and treat a backslash as a slash,
     * so "//evil", "/\\evil" and "/&lt;tab&gt;/evil" would all become a protocol-relative URL.
     */
    private static String ensureSameOriginPath(String path) {
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) < ' ') {
                return "/";
            }
        }
        if (path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
            return "/";
        }
        return path;
    }

}
