package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

/**
 * @author rpuch
 */
public class SSOLogoutPage extends BaseSSOPage {
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SessionService sessionService;

    public SSOLogoutPage() {
        WebRequest request = (WebRequest) getRequest();

        String ssoSessionId = SSOUtils.getSsoSessionIdFromCookie(request);

        if (StringUtils.hasText(ssoSessionId)) {
            sessionService.deleteSSOSession(ssoSessionId);
        }

        String subsystemIdentifier = request.getRequestParameters().getParameterValue("subsystemIdentifier").toString();

        if (StringUtils.hasText(subsystemIdentifier)) {
            UISubsystem subsystem = subsystemService.getSubsystemByName(subsystemIdentifier);
            if (subsystem != null && StringUtils.hasText(subsystem.getSubsystemUrl())) {
                SSOUtils.redirect(this, subsystem.getSubsystemUrl());
            }
        }
    }
}
