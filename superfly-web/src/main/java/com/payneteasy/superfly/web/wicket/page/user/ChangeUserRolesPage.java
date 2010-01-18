package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
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
	@SpringBean
	private SubsystemService subsystemService;
	
	private boolean isWizard;

	public ChangeUserRolesPage(PageParameters params) {
		super(params);
		
		final long userId = params.getAsLong("userId");
		isWizard = params.getAsBoolean("wizard", false);
		
		final Filters filters = new Filters();
		final Form<Filters> filtersForm = new Form<Filters>("filters-form", new Model<Filters>(filters));
		add(filtersForm);
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>("subsystem-filter",
				new PropertyModel<UISubsystemForFilter>(filters, "subsystem"),
				subsystemService.getSubsystemsForFilter(), new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
				
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
		
		final InitializingModel<Collection<UIRoleForCheckbox>> rolesToGrantCheckGroupModel = new InitializingModel<Collection<UIRoleForCheckbox>>() {
			@Override
			protected Collection<UIRoleForCheckbox> getInitialValue() {
				return new HashSet<UIRoleForCheckbox>();
			}
		};
		
		final IDataProvider<UIRoleForCheckbox> rolesProvider = new BaseDataProvider<UIRoleForCheckbox>() {
			public Iterator<? extends UIRoleForCheckbox> iterator(
					int first, int count) {
				UISubsystemForFilter subsystem = filters.getSubsystem();
				List<UIRoleForCheckbox> allUserRoles = userService.getAllUserRoles(userId,
						subsystem == null ? null : subsystem.getId(),
						first, count);
				rolesHolder.setObject(allUserRoles);
				// causing the role check group model to be reinitialized
				rolesCheckGroupModel.clearInitialized();
				rolesToGrantCheckGroupModel.clearInitialized();
				return allUserRoles.iterator();
			}

			public int size() {
				UISubsystemForFilter subsystem = filters.getSubsystem();
				return userService.getAllUserRolesCount(userId,
						subsystem == null ? null : subsystem.getId());
			}
		};
		
		final Form<Void> form = new Form<Void>("form") {
			public void onSubmit() {
				doSubmit(userId, rolesHolder.getObject(), rolesCheckGroupModel.getObject(),
						rolesToGrantCheckGroupModel.getObject());
			}
		};
		add(form);
		final CheckGroup<UIRoleForCheckbox> group = new CheckGroup<UIRoleForCheckbox>("group", rolesCheckGroupModel);
		form.add(group);
		final CheckGroup<UIRoleForCheckbox> grantGroup = new CheckGroup<UIRoleForCheckbox>("grant-group", rolesToGrantCheckGroupModel);
		group.add(grantGroup);
		grantGroup.add(new CheckGroupSelector("master-checkbox", group));
		grantGroup.add(new CheckGroupSelector("grant-master-checkbox", grantGroup));
		DataView<UIRoleForCheckbox> rolesDataView = new PagingDataView<UIRoleForCheckbox>("actions", rolesProvider) {
			@Override
			protected void populateItem(Item<UIRoleForCheckbox> item) {
				UIRoleForCheckbox role = item.getModelObject();
				item.add(new Check<UIRoleForCheckbox>("mapped", item.getModel(), group));
				item.add(new Check<UIRoleForCheckbox>("grant", item.getModel(), grantGroup));
				item.add(new Label("subsystem-name", role.getSubsystemName()));
				item.add(new Label("role-name", role.getRoleName()));
			}
		};
		grantGroup.add(rolesDataView);

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
			Collection<UIRoleForCheckbox> checkedRoles,
			Collection<UIRoleForCheckbox> rolesToGrant) {
		Set<Long> oldCheckedIds = new HashSet<Long>();
		for (UIRoleForCheckbox role : allRoles) {
			if (role.isMapped()) {
				oldCheckedIds.add(role.getId());
			}
		}
		Set<Long> newCheckedIds = new HashSet<Long>();
		for (UIRoleForCheckbox role : checkedRoles) {
			newCheckedIds.add(role.getId());
		}
		Set<Long> idsToAdd = new HashSet<Long>(newCheckedIds);
		idsToAdd.removeAll(oldCheckedIds);
		Set<Long> idsToRemove = new HashSet<Long>(oldCheckedIds);
		idsToRemove.removeAll(newCheckedIds);
		Set<Long> idsToGrant = new HashSet<Long>(rolesToGrant.size());
		for (UIRoleForCheckbox role : rolesToGrant) {
			idsToGrant.add(role.getId());
		}
		
		userService.changeUserRoles(userId, idsToAdd, idsToRemove, idsToGrant);
		
		info("Roles changed");
	}
	
	@Override
	protected String getTitle() {
		return "User roles";
	}
	
	@SuppressWarnings("unused")
	private static class Filters implements Serializable {
		private UISubsystemForFilter subsystem;

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}
	}

}
