package com.payneteasy.superfly.web.wicket.page.group.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
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
		
		//FILTER
		final Filter filter = new Filter();
		final Form<Filter> filtersForm = new Form<Filter>("filters-form", new Model<Filter>(filter));
		add(filtersForm);
		filtersForm.add(new TextField<String>("action-name-substr", new PropertyModel<String>(filter, "actionNameSubstring")));
		
		// SORTABLE DATA PROVIDER
		String[] fieldName = { "actionName" };
		final SortableDataProvider<UIActionForList> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(
				fieldName) {
			
			public Iterator<? extends UIActionForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = groupModel.getGroupSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subsystem.getId());
				List<UIActionForList> list = actionService.getActions(first, count,
						getSortFieldIndex(), isAscending(), filter.getActionNameSubstring(), null,
						subsystem == null ? null : subsystemId);
				setDataset(list);
				return list.iterator(); 
			}

			public int size() {
				UISubsystemForFilter subsystem = groupModel.getGroupSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subsystem.getId());
				return actionService.getActionCount(filter.getActionNameSubstring(), null, subsystem == null ? null : subsystemId);
			}

		};
		
		//CHECKGROUP
		final InitializingModel<Collection<UIActionForList>> actionsCheckGroupModel = new InitializingModel<Collection<UIActionForList>>() {
			@Override
			protected Collection<UIActionForList> getInitialValue() {
				final Collection<UIActionForList> checkedActions = new HashSet<UIActionForList>();
				for (UIActionForList action : ((IndexedSortableDataProvider<UIActionForList>)actionDataProvider).getDataset()) {
					if (action.isSelected()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}
		};
				
		final CheckGroup<UIActionForList> checkGroup = new CheckGroup<UIActionForList>("group", actionsCheckGroupModel);

		// DATAVIEW
		final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>("dataView",actionDataProvider){
			@Override
			protected void populateItem(Item<UIActionForList> item) {
				final UIActionForList action = item.getModelObject();
				item.add(new Label("name",action.getName()));
				item.add(new Label("description",action.getSubsystemName()));
				item.add(new Check("selected", item.getModel(), checkGroup));
			}
			
		};
		
		checkGroup.add(actionDataView);
		checkGroup.add(new OrderByLink("order-by-name", "actionName", actionDataProvider));
		checkGroup.add(new PagingNavigator("paging-navigator", actionDataView));
		checkGroup.add(new CheckGroupSelector("groupselector",checkGroup));
		
		// FORM
		Form<GroupWizardModel> form = new Form<GroupWizardModel>("form", new Model<GroupWizardModel>(groupModel)){
			@Override
			protected void onSubmit() {				
				List<Long> actionsToLink = new ArrayList<Long>();
				List<Long> actionsToUnlink = new ArrayList<Long>();
				List<UIActionForList> allActions = ((IndexedSortableDataProvider<UIActionForList>)actionDataProvider).getDataset();
				Collection<UIActionForList> selectedActions = checkGroup.getModelObject();
				for(UIActionForList e : allActions){
					if(selectedActions.contains(e)){
						actionsToLink.add(e.getId());
					}else{
						actionsToUnlink.add(e.getId());
					}
				}
				groupService.changeGroupActions(curGroup.getId(), actionsToLink, actionsToUnlink);
			}
		};
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
	

	@SuppressWarnings("unused")
	private static class Filter implements Serializable {
		private String actionNameSubstring;

		public String getActionNameSubstring() {
			return actionNameSubstring;
		}

		public void setActionNameSubstring(String actionNameSubstring) {
			this.actionNameSubstring = actionNameSubstring;
		}
	}
}
