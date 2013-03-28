package com.payneteasy.superfly.web.wicket.page.role;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForView;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@Secured("ROLE_ADMIN")
public class ViewRolePage extends BasePage {
	@SpringBean
	private RoleService roleService;
	
	@Override
	protected String getTitle() {
		return "Role details";
	}

	public ViewRolePage(PageParameters param) {
		super(ListRolesPage.class, param);
		
		final Long roleId = param.get("roleid").toLong();
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
		final UIRoleForView curRole = roleService.getRole(roleId);
		
		add(new Label("roleName", curRole.getRoleName()));
		add(new Label("principalName", curRole.getPrincipalName()));
		add(new Label("roleSubsystem", curRole.getRoleName()));
		
		// SORTABLE DATA PROVIDER
		String[] fieldName = { "roleId", "roleName", "subsystemName", "actionId", "actionName" };
		final SortableDataProvider<UIActionForCheckboxForRole, String> actionDataProvider = new IndexedSortableDataProvider<UIActionForCheckboxForRole>(
				fieldName) {
			
			public Iterator<? extends UIActionForCheckboxForRole> iterator(long first, long count) {
				
				List<UIActionForCheckboxForRole> list = roleService.getMappedRoleActions(first, count, 
						getSortFieldIndex(), isAscending(), roleId, filter.getActionNameSubstring());
				return list.iterator(); 
			}

			public long size() {
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
		//add(new PagingNavigator("paging-navigator", actionDataView));
		add(new SuperflyPagingNavigator("paging-navigator", actionDataView));
		
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
