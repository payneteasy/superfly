package com.payneteasy.superfly.web.wicket.page.subsystem;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class EditSubsytemPage extends BasePage {
	@SpringBean
	private SubsystemService subsystemService;

	public EditSubsytemPage(PageParameters parameters) {
       super(parameters);
       long subsystemId = parameters.getAsLong("id", -1L);
       final UISubsystem subsystem = subsystemService.getSubsystem(subsystemId);
       setDefaultModel(new CompoundPropertyModel<UISubsystem>(subsystem));
       Form<UISubsystem> form = new Form<UISubsystem>("form"){

		@Override
		protected void onSubmit() {
			subsystemService.updateSubsystem(subsystem);
			setResponsePage(ListSubsystemsPage.class);
		}
    	   
       };
       add(form);
       form.add(new RequiredTextField<String>("name"));
       form.add(new RequiredTextField<String>("callbackInformation").add(new UrlValidator(new String[]{"http","https"})));
       form.add(new Button("form-submit"));
       form.add(new Button("form-cancel"){

		@Override
		public void onSubmit() {
			setResponsePage(ListSubsystemsPage.class);
		}
    	   
       }.setDefaultFormProcessing(false));
	}

	@Override
	protected String getTitle() {
		return "Edit subsystem";
	}	
	
}
