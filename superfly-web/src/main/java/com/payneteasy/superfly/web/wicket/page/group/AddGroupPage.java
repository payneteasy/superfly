package com.payneteasy.superfly.web.wicket.page.group;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;

public class AddGroupPage extends BasePage {
	@SpringBean
	private GroupService groupService;
	@SpringBean
	private SubsystemService subsystemService;

	public AddGroupPage() {
		final UIGroup group = new UIGroup();
		setDefaultModel(new CompoundPropertyModel<UIGroup>(group));
		final SubsystemModel subsystemModel = new SubsystemModel();
		
		setDefaultModel(new CompoundPropertyModel<SubsystemModel>(
				subsystemModel));
		Form form = new Form("form") {

			@Override
			protected void onSubmit() {
				group.setSubsystemId(subsystemModel.getUiSubsystemForFilter().getId());
				groupService.createGroup(group);
				setResponsePage(ListGroupPage.class);
			}

		};
		add(form);
		form.add(new RequiredTextField<String>("name-group", new PropertyModel<String>(
				group, "name")));
		form.add(new DropDownChoice<UISubsystemForFilter>("uiSubsystemForFilter", subsystemService
				.getSubsystemsForFilter(), new SubsystemChoiceRenderer())
				.setNullValid(true));
		form.add(new Button("cancel") {

			@Override
			public void onSubmit() {
				setResponsePage(ListGroupPage.class);
			}

		}.setDefaultFormProcessing(false));
	}
}
