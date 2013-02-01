package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import org.apache.wicket.protocol.http.WebResponse;

/**
 * @author rpuch
 */
public abstract class AbstractSSOPage extends SessionAccessorPage {
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
