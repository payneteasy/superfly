package com.payneteasy.superfly.web.wicket.page.subsystem;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.field.LabelCheckBoxRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class AddSubsystemPage extends BasePage {
	@SpringBean
	private SubsystemService subsystemService;

	public AddSubsystemPage() {
		super(ListSubsystemsPage.class);
		
		final UISubsystem subsystem = new UISubsystem();
		Form<UISubsystem> form = new Form<UISubsystem>("form",new CompoundPropertyModel<UISubsystem>(subsystem)) {
			@Override
			protected void onSubmit() {
				subsystemService.createSubsystem(subsystem);
				setResponsePage(ListSubsystemsPage.class);
			}

		};
		add(form);
		form.add(new LabelTextFieldRow<UISubsystem>(subsystem,"name","subsystem.add.name",true));
		
		LabelTextFieldRow<String> callbackInformation = new LabelTextFieldRow<String>(subsystem, "callbackInformation", "subsystem.add.callback",true);
		callbackInformation.getTextField().add(new UrlValidator(new String[] {"http", "https"}));
		form.add(callbackInformation);
		
		form.add(new LabelCheckBoxRow("allowListUsers", subsystem, "subsystem.add.allow-list-users"));
		
		form.add(new BookmarkablePageLink<Page>("cancel", ListSubsystemsPage.class));
	}

	@Override
	protected String getTitle() {
		return "Add subsystem";
	}
}
