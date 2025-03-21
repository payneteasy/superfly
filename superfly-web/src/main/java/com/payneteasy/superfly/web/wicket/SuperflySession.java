package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.model.StickyFilters;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginData;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

@Getter
public class SuperflySession extends WebSession {

    private final StickyFilters stickyFilters = new StickyFilters();
    @Setter
    private       SSOLoginData  ssoLoginData;

    public SuperflySession(Request request) {
        super(request);
    }

}
