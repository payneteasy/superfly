package com.payneteasy.superfly.web.wicket.page.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.BaseDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;

@Secured("ROLE_ADMIN")
public class AddRoleGroupsPage extends BasePage {
	@SpringBean
	private RoleService roleService;
	private boolean isWizard;

	public AddRoleGroupsPage(PageParameters params) {
		super(ListRolesPage.class, params);
		final long roleId = params.getAsLong("id", -1);
		isWizard = params.getAsBoolean("wizard", false);
		
		final ObjectHolder<List<UIGroupForCheckbox>> rolesHolder = new ObjectHolder<List<UIGroupForCheckbox>>();
		final InitializingModel<Collection<UIGroupForCheckbox>> rolesCheckGroupModel = new InitializingModel<Collection<UIGroupForCheckbox>>() {

			@Override
			protected Collection<UIGroupForCheckbox> getInitialValue() {
				final Collection<UIGroupForCheckbox> checkedRoles = new HashSet<UIGroupForCheckbox>();
				for (UIGroupForCheckbox role : rolesHolder.getObject()) {
					if (role.isMapped()) {
						checkedRoles.add(role);
					}
				}
				return checkedRoles;
			}

		};

		final IDataProvider<UIGroupForCheckbox> rolesProvider = new BaseDataProvider<UIGroupForCheckbox>() {

			public Iterator<? extends UIGroupForCheckbox> iterator(int first,
					int count) {
				List<UIGroupForCheckbox> allGroupsForRole = roleService
						.getAllRoleGroups(first, count,
								DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
								DaoConstants.ASC, roleId);
				rolesHolder.setObject(allGroupsForRole);
				rolesCheckGroupModel.clearInitialized();
				return allGroupsForRole.iterator();
			}

			public int size() {
				return roleService.getAllRoleGroupsCount(roleId);
			}

		};
		final Form<Void> form = new Form<Void>("form") {
			public void onSubmit() {
				doSubmit(roleId, rolesHolder.getObject(), rolesCheckGroupModel
						.getObject());
			}
		};
		add(form);

		final CheckGroup<UIGroupForCheckbox> group = new CheckGroup<UIGroupForCheckbox>(
				"group", rolesCheckGroupModel);
		form.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		DataView<UIGroupForCheckbox> rolesDataView = new PagingDataView<UIGroupForCheckbox>(
				"actions", rolesProvider) {
			@Override
			protected void populateItem(Item<UIGroupForCheckbox> item) {
				UIGroupForCheckbox role = item.getModelObject();
				item.add(new Check<UIGroupForCheckbox>("mapped", item
						.getModel(), group));
				item.add(new Label("subsystem-name", role.getSubsystemName()));
				item.add(new Label("group-name", role.getGroupName()));
			}
		};
		group.add(rolesDataView);

		form.add(new PagingNavigator("paging-navigator", rolesDataView));

		form.add(new SubmitLink("save-actions-link"));
		form.add(new BookmarkablePageLink<Page>("cancel", ListRolesPage.class));
		
		PageParameters pageParams = new PageParameters();
		pageParams.add("id", String.valueOf(roleId));
		pageParams.add("wizard", "true");
		BookmarkablePageLink<AddRoleActionsPage> nextLink = new BookmarkablePageLink<AddRoleActionsPage>(
				"next-link", AddRoleActionsPage.class, pageParams);
		nextLink.setVisible(isWizard);
		form.add(nextLink);

	}

	protected void doSubmit(long roleId, List<UIGroupForCheckbox> allGroups,
			Collection<UIGroupForCheckbox> checkedGroups) {
		List<Long> idsToAdd = new ArrayList<Long>();
		List<Long> idsToRemove = new ArrayList<Long>();
		for (UIGroupForCheckbox group : allGroups) {
			if (checkedGroups.contains(group)) {
				idsToAdd.add(group.getGroupId());
			} else {
				idsToRemove.add(group.getGroupId());
			}
		}

		RoutineResult result = roleService.changeRoleGroups(roleId, idsToAdd, idsToRemove);
		if (result.isOk()) {
			info("Groups changed; please be aware that some sessions could be invalidated");
		} else {
			error("Error while changing role groups: " + result.getErrorMessage());
		}
	}

	@Override
	protected String getTitle() {
		return "Role Groups";
	}

}
