package com.payneteasy.superfly.web.wicket.page.group.wizard;

import org.springframework.security.annotation.Secured;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class CreateGroupWizardPage extends BasePage {

	@Override
	protected String getTitle() {
		return "Create group";
	}
	
	public CreateGroupWizardPage() {
		add( new CreateGroupWizardPanel("wizard-panel"));
	}
}
