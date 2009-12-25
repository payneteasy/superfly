package com.payneteasy.superfly.demo.web.wicket.page;

import org.apache.wicket.PageParameters;
import org.springframework.security.annotation.Secured;

@Secured({"ROLE_ADMINPAGE2", "ROLE_USERPAGE2"})
public class Page2 extends BasePage {

	public Page2(PageParameters parameters) {
		super(parameters);
	}

}
