package com.payneteasy.superfly.web.wicket.page.group.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.group.SubsystemModel;

@Secured("ROLE_ADMIN")
public class GroupPropertiesWizardStep extends WizardStep {
	@SpringBean
	SubsystemService ssysService;
	
	private static final long serialVersionUID = 1L;

	public GroupPropertiesWizardStep(IModel<String> title, IModel<String> summary,
			IModel<?> model) {
		super(title, summary, model);
		
		//show name textfield
		add(new RequiredTextField("groupName",new PropertyModel(model,"groupName")));
		
		//get subsystems and render them
		DropDownChoice<UISubsystemForFilter> subsystemsList = new DropDownChoice<UISubsystemForFilter>(
				"groupSubsystem", 
				new PropertyModel<UISubsystemForFilter>(model, "groupSubsystem"),				
				ssysService.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer());
		
		subsystemsList.setNullValid(false);
		add(subsystemsList);
		
	}

}
