package com.payneteasy.superfly.demo.web.wicket.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.access.annotation.Secured;

@Secured({"ROLE_ADMINPAGE1", "ROLE_ADMINPAGE2"})
public class AdminPage extends BasePage {

    public AdminPage(PageParameters parameters) {
        super(parameters);
    }

}
