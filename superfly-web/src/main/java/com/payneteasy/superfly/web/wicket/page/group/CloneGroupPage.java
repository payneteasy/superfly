package com.payneteasy.superfly.web.wicket.page.group;

import java.io.Serializable;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.group.UICloneGroupRequest;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class CloneGroupPage extends BasePage {
	@SpringBean
	private GroupService groupService;
	@SpringBean
	private SubsystemService subsystemService;

	@Override
	protected String getTitle() {
		return "Clone group";
	}

	public CloneGroupPage(PageParameters param){
		this(param.getAsLong("sid"));		
	}

	
	public CloneGroupPage(Long sourceId) {
		
		final UIGroup sourceGroup = groupService.getGroupById(sourceId);
		GroupModel groupModel = new GroupModel();
			
		Form<GroupModel> form = new Form<GroupModel>("form", new Model(groupModel)) {
			@Override
			protected void onSubmit() {
				UICloneGroupRequest request = new UICloneGroupRequest();
				request.setNewGroupName(getModelObject().getName());
				request.setSourceGroupId(sourceGroup.getId());
				groupService.cloneGroup(request);
				setResponsePage(ListGroupsPage.class);
			}

		};
		add(form);
		
		form.add(new Label("source-label",sourceGroup.getLabel()));
		form.add(new RequiredTextField<String>("group-name",new PropertyModel<String>(groupModel,"name")));
		form.add(new Button("cancel") {
			@Override
			public void onSubmit() {
				setResponsePage(ListGroupsPage.class);
			}

		}.setDefaultFormProcessing(false));
	}
	
	private class GroupModel implements Serializable{
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}

}
