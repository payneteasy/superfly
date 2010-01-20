package com.payneteasy.superfly.web.wicket.page.group.wizard;

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;

@Secured("ROLE_ADMIN")
public class GroupPropertiesPage extends BasePage {
	@SpringBean
	SubsystemService ssysService;

	@SpringBean
	GroupService groupService;
	
	@Override
	protected String getTitle() {
		return "Group properties";
	}

	public GroupPropertiesPage() {
		this((Long)null);
	}

	public GroupPropertiesPage(PageParameters param){
		this(param.getAsLong("gid"));		
	}

	public GroupPropertiesPage(final Long groupId) {		
		String msg_text="Please, provide new Group name and Subsystem";
		GroupWizardModel groupModel = new GroupWizardModel();
		
		// edit || create
		if(groupId!=null){
			msg_text = "Edit Group name";
			UIGroup group = groupService.getGroupById(groupId);
			groupModel.setGroupName(group.getName());
			List<UISubsystemForFilter> list = ssysService.getSubsystemsForFilter();
			for(UISubsystemForFilter e: list){
				if(e.getId() == group.getSubsystemId())groupModel.setGroupSubsystem(e);
			}
			
		}
		
		Form<GroupWizardModel> form = new Form<GroupWizardModel>("form", new Model<GroupWizardModel>(groupModel)){
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit() {
				GroupWizardModel grModel = this.getModelObject();
				UIGroup group = new UIGroup();
				group.setName(grModel.getGroupName());
				group.setSubsystemId(grModel.getGroupSubsystem().getId());
				if(groupId == null){
					groupService.createGroup(group);
					PageParameters params = new PageParameters();
					params.add("gid", String.valueOf(groupId==null ? group.getId(): groupId));
					setResponsePage(GroupActionsPage.class,params);
				}else{
					groupService.updateGroup(group);
					setResponsePage(ListGroupsPage.class);
				}
				
			}
		};
		
		form.add(new Label("msg",msg_text));
		form.add(new RequiredTextField("groupName",new PropertyModel(groupModel,"groupName")));
		form.add(new Button("btn-cancel"){
			@Override
			public void onSubmit() {
				setResponsePage(ListGroupsPage.class);
			}
		}.setDefaultFormProcessing(false));
		
		//get subsystems and render them
		DropDownChoice<UISubsystemForFilter> subsystemsList = new DropDownChoice<UISubsystemForFilter>(
				"groupSubsystem", 
				new PropertyModel<UISubsystemForFilter>(groupModel, "groupSubsystem"),				
				ssysService.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer());
		
		subsystemsList.setNullValid(false);
		if(groupId!=null)subsystemsList.setEnabled(false);
		form.add(subsystemsList);
		add(form);

	}


}
