package com.payneteasy.superfly.web.page.group;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.UIGroup;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.page.BasePage;

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
				setResponsePage(GroupListPage.class);
			}

		};
		add(form);
		form.add(new RequiredTextField<String>("name-group", new PropertyModel(
				group, "name")));
		form.add(new DropDownChoice("uiSubsystemForFilter", subsystemService
				.getSubsystemsForFilter(), new SubsystemChoiceRenderer())
				.setNullValid(true));
		form.add(new Button("cancel") {

			@Override
			public void onSubmit() {
				setResponsePage(GroupListPage.class);
			}

		}.setDefaultFormProcessing(false));
	}
}
