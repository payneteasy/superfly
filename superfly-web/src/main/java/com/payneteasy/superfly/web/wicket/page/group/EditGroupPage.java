package com.payneteasy.superfly.web.wicket.page.group;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupWizardModel;

@Secured("ROLE_ADMIN")
public class EditGroupPage extends BasePage {
	@SpringBean
	SubsystemService ssysService;

	@SpringBean
	GroupService groupService;

	public EditGroupPage(PageParameters param) {
		super(ListGroupsPage.class, param);
		String msg_text = "Edit Group name";
		final Long groupId = param.getAsLong("gid");

		GroupWizardModel groupModel = new GroupWizardModel();

		UIGroup group = groupService.getGroupById(groupId);
		groupModel.setGroupName(group.getName());
		List<UISubsystemForFilter> list = ssysService.getSubsystemsForFilter();
		for (UISubsystemForFilter e : list) {
			if (e.getId() == group.getSubsystemId())
				groupModel.setGroupSubsystem(e);
		}

		Form<GroupWizardModel> form = new Form<GroupWizardModel>("form", new Model<GroupWizardModel>(groupModel)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				GroupWizardModel grModel = this.getModelObject();
				UIGroup group = new UIGroup();
				group.setName(grModel.getGroupName());
				group.setSubsystemId(grModel.getGroupSubsystem().getId());
				group.setId(groupId);
				groupService.updateGroup(group);
				setResponsePage(ListGroupsPage.class);

			}
		};
		add(form);

		form.add(new Label("msg", msg_text));
		form.add(new LabelTextFieldRow<String>(groupModel, "groupName", "group.create.name", true));
		
		form.add(new BookmarkablePageLink<Page>("btn-cancel", ListGroupsPage.class));
		
		LabelDropDownChoiceRow<UISubsystemForFilter> subsystem = new LabelDropDownChoiceRow<UISubsystemForFilter>("groupSubsystem",
				groupModel, "group.create.choice-subsystem", ssysService.getSubsystemsForFilter(), new SubsystemChoiceRenderer());
		subsystem.getDropDownChoice().setRequired(true);
		subsystem.getDropDownChoice().setEnabled(false);
		form.add(subsystem);
	}

	@Override
	protected String getTitle() {
		return "Edit group";
	}

}
