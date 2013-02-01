package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

/**
 * @author rpuch
 */
public class SSOLogoutPage extends AbstractSSOPage {
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SessionService sessionService;

    public SSOLogoutPage() {
        WebRequest request = (WebRequest) getRequest();
        String returnUrl = request.getParameter("returnUrl");

        String ssoSessionId = SSOUtils.getSsoSessionIdFromCookie(request);

        if (StringUtils.hasText(ssoSessionId)) {
            sessionService.deleteSSOSession(ssoSessionId);
        }

        SSOUtils.redirect(this, returnUrl);
    }
}
