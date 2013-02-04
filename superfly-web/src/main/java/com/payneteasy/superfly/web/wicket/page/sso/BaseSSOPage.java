package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebResponse;

/**
 * @author rpuch
 */
public abstract class BaseSSOPage extends SessionAccessorPage {
    protected BaseSSOPage() {
        SSOLoginData loginData = getSession().getSsoLoginData();
        WebMarkupContainer subsystemInfoContainer = new WebMarkupContainer("subsystem-info");
        boolean subsystemInfoVisible = isSubsystemInfoShown() && loginData != null;
        subsystemInfoContainer.setVisible(subsystemInfoVisible);
        add(subsystemInfoContainer);

        if (subsystemInfoVisible) {
            subsystemInfoContainer.add(new Label("subsystem-title", loginData.getSubsystemTitle()));
            subsystemInfoContainer.add(new Label("subsystem-url", loginData.getSubsystemUrl()));
        }
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

    protected boolean isSubsystemInfoShown() {
        return true;
    }
}
