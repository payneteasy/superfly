package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

/**
 * @author rpuch
 */
public class SSOLoginPage extends BaseSSOPage {
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
            targetUrl = sanitizeTargetUrl(targetUrl);

            SSOLoginData loginData = new SSOLoginData(subsystemIdentifier, targetUrl);
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
                        String reason = String.format("No subsystem token for sso_session_id '%s' and subsystemIdentifier = '%s', ssoSession = %s"
                                , ssoSessionId, subsystemIdentifier, ssoSession);
                        SSOUtils.redirectToCantLoginErrorPage(this, loginData, reason);
                    }
                    needToLogin = false;
                }
            }

            if (needToLogin) {
                UISubsystem subsystem = subsystemService.getSubsystemByName(subsystemIdentifier);
                loginData.setSubsystemTitle(subsystem.getTitle());
                loginData.setSubsystemUrl(subsystem.getSubsystemUrl());
                getRequestCycle().setResponsePage(new SSOLoginPasswordPage());
            }
        }
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

}
