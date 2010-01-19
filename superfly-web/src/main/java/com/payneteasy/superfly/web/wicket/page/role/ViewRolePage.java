package com.payneteasy.superfly.web.wicket.page.role;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

@Secured("ROLE_ADMIN")
public class ViewRolePage extends BasePage {
	@SpringBean
	ActionService actionService;
	
	@SpringBean
	SubsystemService ssysService;
	
	@SpringBean
	RoleService roleService;
	
	@Override
	protected String getTitle() {
		return "Role details";
	}
	
	public ViewRolePage(PageParameters param){
		this(param.getAsLong("roleid"));		
	}

	public ViewRolePage(final Long roleId) {
		
		//BACK
		Form<Void> formBack = new Form<Void>("back-form");
		formBack.add(new Button("btn-back"){
			@Override
			public void onSubmit() {
				setResponsePage(ListRolesPage.class);
			}
		}.setDefaultFormProcessing(false));
		add(formBack);
		
		//FILTER
		final Filter filter = new Filter();
		final Form<Filter> filtersForm = new Form<Filter>("filter-form", new Model<Filter>(filter));
		add(filtersForm);
		filtersForm.add(new TextField<String>("action-name-substr", new PropertyModel<String>(filter, "actionNameSubstring")));
		
		//ROLE PROPERTIES
		final UIRole curRole = roleService.getRole(roleId);
		
		add(new Label("roleName", curRole.getRoleName()));
		add(new Label("principalName", curRole.getPrincipalName()));
		String ssysName = "";
		List<UISubsystemForFilter> list = ssysService.getSubsystemsForFilter();
		for (UISubsystemForFilter e : list) {
			if (e.getId() == curRole.getSubsystemId()) {
				ssysName = e.getName();
			}
		}
		add(new Label("roleSubsystem", ssysName));
		
		
		// SORTABLE DATA PROVIDER
		String[] fieldName = { "roleId", "roleName", "subsystemName", "actionId", "actionName" };
		final SortableDataProvider<UIActionForCheckboxForRole> actionDataProvider = new IndexedSortableDataProvider<UIActionForCheckboxForRole>(
				fieldName) {
			
			public Iterator<? extends UIActionForCheckboxForRole> iterator(int first,
					int count) {
				
				List<UIActionForCheckboxForRole> list = roleService.getMappedRoleActions(first, count, 
						getSortFieldIndex(), isAscending(), roleId, filter.getActionNameSubstring());
				return list.iterator(); 
			}

			public int size() {
				return roleService.getMappedRoleActionsCount(roleId, filter.getActionNameSubstring());
			}

		};
		

		// DATAVIEW
		final DataView<UIActionForCheckboxForRole> actionDataView = new PagingDataView<UIActionForCheckboxForRole>("dataView", actionDataProvider){
			@Override
			protected void populateItem(Item<UIActionForCheckboxForRole> item) {
				final UIActionForCheckboxForRole action = item.getModelObject();
				item.add(new Label("action-id",String.valueOf(action.getActionId())));
				item.add(new Label("action-name",action.getActionName()));
			}
			
		};
		
		add(actionDataView);
		add(new OrderByLink("order-by-ActionID", "actionId", actionDataProvider));
		add(new OrderByLink("order-by-ActionName", "actionName", actionDataProvider));
		add(new PagingNavigator("paging-navigator", actionDataView));
		
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
