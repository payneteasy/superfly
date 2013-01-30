package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;

/**
 * @author rpuch
 */
public class SSOLoginPage extends SessionAccessorPage {
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SessionService sessionService;

    public SSOLoginPage() {
        WebRequest request = (WebRequest) getRequest();
        String subsystemIdentifier = request.getParameter("subsystemIdentifier");
        String targetUrl = sanitizeTargetUrl(request.getParameter("targetUrl"));

        SSOLoginData loginData = new SSOLoginData();
        loginData.setSubsystemIdentifier(subsystemIdentifier);
        loginData.setTargetUrl(targetUrl);

        saveLoginData(loginData);

        String ssoSessionId = null;
        Cookie cookie = request.getCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME);
        if (cookie != null) {
            ssoSessionId = cookie.getValue();
        }
        boolean needToLogin = true;
        if (StringUtils.hasText(ssoSessionId)) {
            SSOSession ssoSession = sessionService.getValidSSOSession(ssoSessionId);
            if (ssoSession != null) {
                // session is valid
                SubsystemTokenData token = subsystemService.issueSubsystemTokenIfCanLogin(ssoSession.getId(),
                        subsystemIdentifier);
                if (token != null) {
                    // can login: redirecting a user to a subsystem
                    getRequestCycle().setRequestTarget(new RedirectRequestTarget(SSOUtils.buildRedirectToSubsystemUrl(token.getLandingUrl(), token.getSubsystemToken(), targetUrl)));
                } else {
                    // can't login: just display an error
                    SSOUtils.redirectToCantLoginErrorPage(this, loginData);
                }
                needToLogin = false;
            }
        }

        if (needToLogin) {
            getRequestCycle().setResponsePage(new SSOLoginPasswordPage());
            getRequestCycle().setRedirect(true);
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

    private void saveLoginData(SSOLoginData loginData) {
        getSession().setSsoLoginData(loginData);
    }

    @Override
   	protected void configureResponse() {
   		super.configureResponse();
   		WebResponse response = getWebRequestCycle().getWebResponse();
   		response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");

   		//for IE
   		response.setHeader("Expires", "-1");
   		response.setHeader("Pragma", "no-cache");
   	}

}
