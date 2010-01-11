package com.payneteasy.superfly.web.wicket.page.group.wizard;

import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.security.annotation.Secured;

@Secured("ROLE_ADMIN")
public class GroupPropertiesWizardStep extends WizardStep {

	private static final long serialVersionUID = 1L;

	public GroupPropertiesWizardStep(Model model, Model model2,
			IModel<GroupWizardModel> groupModel) {
		// TODO Auto-generated constructor stub
	}

}
