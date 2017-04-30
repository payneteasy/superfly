package com.payneteasy.superfly.demo.web.wicket.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.access.annotation.Secured;

@Secured({"ROLE_USERPAGE1", "ROLE_USERPAGE2"})
public class UserPage extends BasePage {

    public UserPage(PageParameters parameters) {
        super(parameters);
    }

}
