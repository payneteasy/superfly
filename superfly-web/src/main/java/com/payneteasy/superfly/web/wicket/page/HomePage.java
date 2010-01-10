package com.payneteasy.superfly.web.wicket.page;

import org.springframework.security.annotation.Secured;

@Secured("ROLE_ADMIN")
public class HomePage extends BasePage{

	@Override
	protected String getTitle() {
		return "Superfly dashboard";
	}

}
