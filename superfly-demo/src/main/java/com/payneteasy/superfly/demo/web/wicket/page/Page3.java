package com.payneteasy.superfly.demo.web.wicket.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.access.annotation.Secured;

@Secured({"ROLE_ADMINPAGE3", "ROLE_USERPAGE3"})
public class Page3 extends BasePage {

    public Page3(PageParameters parameters) {
        super(parameters);
    }

}
