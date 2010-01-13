package com.payneteasy.superfly.web.wicket.page.user;

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
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.BaseDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;

/**
 * Used to change roles assigned to a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ChangeUserRolesPage extends BasePage {
	
	@SpringBean
	private UserService userService;
	
	private boolean isWizard;

	public ChangeUserRolesPage(PageParameters params) {
		super(params);
		
		final long userId = params.getAsLong("userId");
		isWizard = params.getAsBoolean("wizard", false);
				
		final ObjectHolder<List<UIRoleForCheckbox>> rolesHolder = new ObjectHolder<List<UIRoleForCheckbox>>();
		
		final InitializingModel<Collection<UIRoleForCheckbox>> rolesCheckGroupModel = new InitializingModel<Collection<UIRoleForCheckbox>>() {
			@Override
			protected Collection<UIRoleForCheckbox> getInitialValue() {
				final Collection<UIRoleForCheckbox> checkedRoles = new HashSet<UIRoleForCheckbox>();
				for (UIRoleForCheckbox role : rolesHolder.getObject()) {
					if (role.isMapped()) {
						checkedRoles.add(role);
					}
				}
				return checkedRoles;
			}
		};
		
		final IDataProvider<UIRoleForCheckbox> rolesProvider = new BaseDataProvider<UIRoleForCheckbox>() {
			public Iterator<? extends UIRoleForCheckbox> iterator(
					int first, int count) {
				List<UIRoleForCheckbox> allUserRoles = userService.getAllUserRoles(userId,
						first, count);
				rolesHolder.setObject(allUserRoles);
				// causing the role check group model to be reinitialized
				rolesCheckGroupModel.clearInitialized();
				return allUserRoles.iterator();
			}

			public int size() {
				return userService.getAllUserRolesCount(userId);
			}
		};
		
		final Form<Void> form = new Form<Void>("form") {
			public void onSubmit() {
				doSubmit(userId, rolesHolder.getObject(), rolesCheckGroupModel.getObject());
			}
		};
		add(form);
		final CheckGroup<UIRoleForCheckbox> group = new CheckGroup<UIRoleForCheckbox>("group", rolesCheckGroupModel);
		form.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		DataView<UIRoleForCheckbox> rolesDataView = new PagingDataView<UIRoleForCheckbox>("actions", rolesProvider) {
			@Override
			protected void populateItem(Item<UIRoleForCheckbox> item) {
				UIRoleForCheckbox role = item.getModelObject();
				item.add(new Check<UIRoleForCheckbox>("mapped", item.getModel(), group));
				item.add(new Label("subsystem-name", role.getSubsystemName()));
				item.add(new Label("role-name", role.getRoleName()));
			}
		};
		group.add(rolesDataView);

		form.add(new PagingNavigator("paging-navigator", rolesDataView));
		
		PageParameters pageParams = new PageParameters();
		pageParams.add("userId", String.valueOf(userId));
		pageParams.add("wizard", "true");
		BookmarkablePageLink<ChangeUserActionsPage> nextLink = new BookmarkablePageLink<ChangeUserActionsPage>(
				"next-link", ChangeUserActionsPage.class, pageParams);
		nextLink.setVisible(isWizard);
		form.add(nextLink);
		
		SubmitLink submitLink = new SubmitLink("submit-link");
		form.add(submitLink);
		form.add(new BookmarkablePageLink<Page>("cancel-link", ListUsersPage.class));
	}

	protected void doSubmit(long userId, List<UIRoleForCheckbox> allRoles,
			Collection<UIRoleForCheckbox> checkedRoles) {
		List<Long> idsToAdd = new ArrayList<Long>();
		List<Long> idsToRemove = new ArrayList<Long>();
		for (UIRoleForCheckbox role : allRoles) {
			if (checkedRoles.contains(role)) {
				idsToAdd.add(role.getId());
			} else {
				idsToRemove.add(role.getId());
			}
		}
		
		userService.changeUserRoles(userId, idsToAdd, idsToRemove);
				
		info("Roles changed");
	}
	
	@Override
	protected String getTitle() {
		return "User roles";
	}

}
