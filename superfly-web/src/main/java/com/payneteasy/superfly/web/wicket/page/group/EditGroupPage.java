package com.payneteasy.superfly.web.wicket.page.group;

import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class EditGroupPage extends BasePage{

	@Override
	protected String getTitle() {
		return "Edit group";
	}

}
