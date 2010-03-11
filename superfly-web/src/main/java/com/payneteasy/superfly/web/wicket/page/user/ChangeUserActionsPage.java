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
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
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
 * Used to change actions assigned to a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ChangeUserActionsPage extends BasePage {
	
	@SpringBean
	private UserService userService;
	@SpringBean
	private SubsystemService subsystemService;

	public ChangeUserActionsPage(PageParameters params) {
		super(params);
		
		final long userId = params.getAsLong("userId");
		final boolean isWizard = params.getAsBoolean("wizard", false);
		
		final Filters filters = new Filters();
		final Form<Filters> filtersForm = new Form<Filters>("filters-form", new Model<Filters>(filters));
		add(filtersForm);
		filtersForm.add(new TextField<String>("action-name-substring",
				new PropertyModel<String>(filters, "actionNameSubstring")));
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>("subsystem-filter",
				new PropertyModel<UISubsystemForFilter>(filters, "subsystem"),
				subsystemService.getSubsystemsForFilter(), new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
		filtersForm.add(new CheckBox("unmapped-only", new PropertyModel<Boolean>(filters, "unmappedOnly")));
		
		final ObjectHolder<List<UIActionForCheckboxForUser>> actionsHolder = new ObjectHolder<List<UIActionForCheckboxForUser>>();
		
		final InitializingModel<Collection<UIActionForCheckboxForUser>> actionsCheckGroupModel = new InitializingModel<Collection<UIActionForCheckboxForUser>>() {
			@Override
			protected Collection<UIActionForCheckboxForUser> getInitialValue() {
				final Collection<UIActionForCheckboxForUser> checkedActions = new HashSet<UIActionForCheckboxForUser>();
				for (UIActionForCheckboxForUser action : actionsHolder.getObject()) {
					if (action.isMapped()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}
		};
		
		final IDataProvider<UIActionForCheckboxForUser> actionsProvider = new BaseDataProvider<UIActionForCheckboxForUser>() {

			public Iterator<? extends UIActionForCheckboxForUser> iterator(
					int first, int count) {
				UISubsystemForFilter subsystem = filters.getSubsystem();
				List<UIActionForCheckboxForUser> userActions;
				if (filters.isUnmappedOnly()) {
					userActions = userService.getUnmappedUserActions(userId,
							subsystem == null ? null : subsystem.getId(),
							filters.getActionNameSubstring(), first, count);
				} else {
					userActions = userService.getAllUserActions(userId,
							subsystem == null ? null : subsystem.getId(),
							filters.getActionNameSubstring(), first, count);
				}
				actionsHolder.setObject(userActions);
				// causing the action check group model to be reinitialized
				actionsCheckGroupModel.clearInitialized();
				return userActions.iterator();
			}

			public int size() {
				UISubsystemForFilter subsystem = filters.getSubsystem();
				if (filters.isUnmappedOnly()) {
					return userService.getUnmappedUserActionsCount(userId,
							subsystem == null ? null : subsystem.getId(),
							filters.getActionNameSubstring());
				} else {
					return userService.getAllUserActionsCount(userId,
							subsystem == null ? null : subsystem.getId(),
							filters.getActionNameSubstring());
				}
			}
		};
		
		final Form<Void> form = new Form<Void>("form") {
			public void onSubmit() {
				doSubmit(userId, actionsHolder.getObject(), actionsCheckGroupModel.getObject());
			}
		};
		add(form);
		final CheckGroup<UIActionForCheckboxForUser> group = new CheckGroup<UIActionForCheckboxForUser>("group", actionsCheckGroupModel);
		form.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		DataView<UIActionForCheckboxForUser> actionsDataView = new PagingDataView<UIActionForCheckboxForUser>("actions", actionsProvider) {
			@Override
			protected void populateItem(Item<UIActionForCheckboxForUser> item) {
				UIActionForCheckboxForUser action = item.getModelObject();
				item.add(new Check<UIActionForCheckboxForUser>("mapped", item.getModel(), group));
				item.add(new Label("subsystem-name", action.getSubsystemName()));
				item.add(new Label("role-name", action.getRoleName()));
				item.add(new Label("action-name", action.getActionName()));
			}
		};
		group.add(actionsDataView);

		form.add(new PagingNavigator("paging-navigator", actionsDataView));
		
		PageParameters pageParams = new PageParameters();
		pageParams.add("userId", String.valueOf(userId));
		pageParams.add("wizard", "true");
		BookmarkablePageLink<ChangeUserRolesPage> backLink = new BookmarkablePageLink<ChangeUserRolesPage>(
				"back-link", ChangeUserRolesPage.class, pageParams);
		backLink.setVisible(isWizard);
		form.add(backLink);
		
		form.add(new SubmitLink("save-link"));
		form.add(new BookmarkablePageLink<Page>("cancel-link", ListUsersPage.class));
	}

	protected void doSubmit(long userId,
			List<UIActionForCheckboxForUser> allActions,
			Collection<UIActionForCheckboxForUser> checkedActions) {
		Set<Long> oldCheckedIds = new HashSet<Long>();
		for (UIActionForCheckboxForUser action : allActions) {
			if (action.isMapped()) {
				oldCheckedIds.add(action.getRoleActionId());
			}
		}
		Set<Long> newCheckedIds = new HashSet<Long>();
		for (UIActionForCheckboxForUser action : checkedActions) {
			newCheckedIds.add(action.getRoleActionId());
		}
		Set<Long> idsToAdd = new HashSet<Long>(newCheckedIds);
		idsToAdd.removeAll(oldCheckedIds);
		Set<Long> idsToRemove = new HashSet<Long>(oldCheckedIds);
		idsToRemove.removeAll(newCheckedIds);
		
		RoutineResult result = userService.changeUserRoleActions(userId, idsToAdd, idsToRemove);
		if (result.isOk()) {
			info("Actions changed; please be aware that some sessions could be invalidated");
		} else {
			error("Error while changing user actions: " + result.getErrorMessage());
		}
	}
	
	@Override
	protected String getTitle() {
		return "User actions";
	}

	@SuppressWarnings("unused")
	private static class Filters implements Serializable {
		private String actionNameSubstring;
		private UISubsystemForFilter subsystem;
		private boolean unmappedOnly = false;

		public String getActionNameSubstring() {
			return actionNameSubstring;
		}

		public void setActionNameSubstring(String actionNameSubstring) {
			this.actionNameSubstring = actionNameSubstring;
		}

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}

		public boolean isUnmappedOnly() {
			return unmappedOnly;
		}

		public void setUnmappedOnly(boolean unmappedOnly) {
			this.unmappedOnly = unmappedOnly;
		}
	}

}
