package com.payneteasy.superfly.demo.web.wicket.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.access.annotation.Secured;

@Secured({"ROLE_ADMINPAGE2", "ROLE_USERPAGE2"})
public class Page2 extends BasePage {

    public Page2(PageParameters parameters) {
        super(parameters);
    }

}
