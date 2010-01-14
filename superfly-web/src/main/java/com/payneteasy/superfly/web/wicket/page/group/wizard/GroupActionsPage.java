package com.payneteasy.superfly.web.wicket.page.group.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

@Secured("ROLE_ADMIN")
public class GroupActionsPage extends BasePage {
	@SpringBean
	ActionService actionService;
	
	@SpringBean
	SubsystemService ssysService;
	
	@SpringBean
	GroupService groupService;
	
	@Override
	protected String getTitle() {
		return "Group actions";
	}
	
	public GroupActionsPage() {
		this((Long)null);
	}
	
	public GroupActionsPage(PageParameters param){
		this(param.getAsLong("gid"));		
	}

	public GroupActionsPage(Long groupId) {
		
		final GroupWizardModel groupModel = new GroupWizardModel();

		final UIGroup curGroup = groupService.getGroupById(groupId);
		groupModel.setGroupName(curGroup.getName());
		List<UISubsystemForFilter> list = ssysService.getSubsystemsForFilter();
		for(UISubsystemForFilter e: list){
			if(e.getId() == curGroup.getSubsystemId())groupModel.setGroupSubsystem(e);
		}
		
		// SORTABLE DATA PROVIDER

		// init selected actions list
		groupModel.setActions(new ArrayList<UIActionForList>());
		
		Form<GroupWizardModel> form = new Form<GroupWizardModel>("form", new Model<GroupWizardModel>(groupModel)){
			//onSubmit Form override
		};
		
		form.add(new Label("msg","Edit actions for group"));
		String[] fieldName = { "actionName" };
		final SortableDataProvider<UIActionForList> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(
				fieldName) {
						
			public Iterator<? extends UIActionForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = groupModel.getGroupSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subsystem.getId());
				List<UIActionForList> list = actionService.getActions(first, count,
						getSortFieldIndex(), isAscending(), null, null,
						subsystem == null ? null : subsystemId);
				return list.iterator(); 
			}

			public int size() {
				UISubsystemForFilter subsystem = groupModel.getGroupSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subsystem.getId());
				return actionService.getActionCount(null, null, subsystem == null ? null : subsystemId);
			}

		};
		
		// DATAVIEW
		final CheckGroup checkGroup = new CheckGroup("group", groupModel.getActions());
		
		final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>("dataView",actionDataProvider){
			@Override
			protected void populateItem(Item<UIActionForList> item) {
				final UIActionForList action = item.getModelObject();
				item.add(new Label("name",action.getName()));
				item.add(new Label("description",action.getSubsystemName()));
				item.add(new Check("selected", new PropertyModel(item.getModel(), "selected")));
			}
			
		};
	
		
		checkGroup.add(actionDataView);
		checkGroup.add(new OrderByLink("order-by-name", "actionName", actionDataProvider));
		checkGroup.add(new PagingNavigator("paging-navigator", actionDataView));
		
		checkGroup.add(new CheckGroupSelector("groupselector"));
		
		form.add(new Button("btn-back"){
			@Override
			public void onSubmit() {
				PageParameters params = new PageParameters();
				params.add("gid", String.valueOf(curGroup.getId()));
				setResponsePage(GroupPropertiesPage.class,params);
			}
		}.setDefaultFormProcessing(false));
		
		form.add(new Button("btn-cancel"){
			@Override
			public void onSubmit() {
				setResponsePage(ListGroupsPage.class);
			}
		}.setDefaultFormProcessing(false));
		
		form.add(checkGroup);
		add(form);
		//---
	}
	

}
