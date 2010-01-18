package com.payneteasy.superfly.web.wicket.page.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.component.EmptyPanel;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;

@Secured("ROLE_ADMIN")
public class ListRolesPage extends BasePage {
	@SpringBean
	private RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;

	public ListRolesPage() {
		super();
		add(new EmptyPanel("confirmPanel"));
		
		final RoleFilter roleFilter = new RoleFilter();
		Form<RoleFilter> filtersForm = new Form("filters-form");
		add(filtersForm);
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
				"subsystem-filter", new PropertyModel<UISubsystemForFilter>(
						roleFilter, "subsystem"), subsystemService
						.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
		
		final ObjectHolder<List<UIRoleForList>> rolesHolder = new ObjectHolder<List<UIRoleForList>>();
		final InitializingModel<Collection<UIRoleForList>> rolesCheckGroupModel = new InitializingModel<Collection<UIRoleForList>>() {

			@Override
			protected Collection<UIRoleForList> getInitialValue() {
				final Collection<UIRoleForList> checkedRoles = new HashSet<UIRoleForList>();
				for (UIRoleForList role : rolesHolder
						.getObject()) {
					if (role.isSelected()) {
						checkedRoles.add(role);
					}
				}
				return checkedRoles;
			}

		};
		String[] fieldNames = { "roleId", "roleName", "principalName","subsystemName" };
		SortableDataProvider<UIRoleForList> rolesDataProvider = new IndexedSortableDataProvider<UIRoleForList>(
				fieldNames) {

			public Iterator<? extends UIRoleForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = roleFilter.getSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				if (subsystem == null) {
					List<UIRoleForList> roles = roleService.getRoles(first, count,
							getSortFieldIndex(), isAscending(), null,
							subsystem == null ? null : null);
					rolesHolder.setObject(roles);
					rolesCheckGroupModel.clearInitialized();
					return roles.iterator();
				} else {
					subsystemId.add(subsystem.getId());
					List<UIRoleForList> roles = roleService.getRoles(first, count,
							getSortFieldIndex(), isAscending(), null,
							subsystem == null ? null : subsystemId);
					rolesHolder.setObject(roles);
					rolesCheckGroupModel.clearInitialized();
					return roles.iterator();
				}
			}

			public int size() {
				UISubsystemForFilter subsystem = roleFilter.getSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				if (subsystem == null) {
					return roleService.getRoleCount(null,
							subsystem == null ? null : null);
				} else {
					subsystemId.add(subsystem.getId());
					return roleService.getRoleCount(null,
							subsystem == null ? null : subsystemId);
				}
			}

		};
		final Form<Void> form = new Form<Void>("form") {
		};
		add(form);
		final CheckGroup<UIRoleForList> group = new CheckGroup<UIRoleForList>(
				"group", rolesCheckGroupModel);
		form.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		
		DataView<UIRoleForList> rolesDateView = new PagingDataView<UIRoleForList>(
				"roleList", rolesDataProvider) {

			@Override
			protected void populateItem(Item<UIRoleForList> item) {
				final UIRoleForList role = item.getModelObject();
				item.add(new Label("role-name", role.getName()));
				item.add(new Label("principal-name",role.getPrincipalName()));
				item.add(new Label("subsystem-name", role.getSubsystem()));
				item.add(new Check<UIRoleForList>("selected", item.getModel(),group));
				item.add(new BookmarkablePageLink("role-edit",
						EditRolePage.class).setParameter("id", role.getId()));
				item.add(new BookmarkablePageLink("role-groups",
						ChangeRoleGroupsPage.class).setParameter("id", role.getId()));
				item.add(new BookmarkablePageLink("role-actions",
						ChangeRoleActionsPage.class).setParameter("id", role.getId()));
			}

		};
		group.add(rolesDateView);
		group.add(new OrderByLink("order-by-roleName", "roleName", rolesDataProvider));
		group.add(new OrderByLink("order-by-principalName", "principalName", rolesDataProvider));
		group.add(new OrderByLink("order-by-subsystemName", "subsystemName",
				rolesDataProvider));
		form.add(new PagingNavigator("paging-navigator", rolesDateView));
		form.add(new Button("delete-role"){

			@Override
			public void onSubmit() {
				final Collection<UIRoleForList> checkedRoles=rolesCheckGroupModel.getObject();
				final List<UIRoleForList> roles = rolesHolder.getObject();
				if(checkedRoles.size()==0)return;
				
				this.getPage().get("confirmPanel").replaceWith(
						new ConfirmPanel("confirmPanel",
								"You are about to delete "+checkedRoles.size()
										+ " role(s) permanently?") {
							public void onConfirm() {
								for(UIRoleForList uir: roles){
									if(checkedRoles.contains(uir)){
										
										roleService.deleteRole(uir.getId());
									}
								}
								this.getPage().setResponsePage(ListRolesPage.class);
							}

							public void onCancel() {
								this.getPage().get("confirmPanel").replaceWith(
										new EmptyPanel("confirmPanel"));
							}
						});
			}
			
		});
		add(new BookmarkablePageLink<AddRolePage>("add-role", AddRolePage.class));
	}
	
	@Override
	protected String getTitle() {
		return "Roles";
	}

	@SuppressWarnings("unused")
	private class RoleFilter implements Serializable {
		private UISubsystemForFilter subsystem;

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}

	}
}