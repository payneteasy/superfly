package com.payneteasy.superfly.web.wicket.page.role;

import java.io.Serializable;
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

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.BaseDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;
@Secured("ROLE_ADMIN")
public class ChangeRoleActionsPage extends BasePage {
	@SpringBean
	private RoleService roleService;

	public ChangeRoleActionsPage(PageParameters parameters) {
		super(parameters);
		final long roleId = parameters.getAsLong("id", -1);
		final Filters filters = new Filters();
		final Form<Filters> filtersForm = new Form<Filters>("filters-form",
				new Model<Filters>(filters));
		add(filtersForm);
		filtersForm.add(new TextField<String>("action-name-substring",
				new PropertyModel<String>(filters, "actionNameSubstring")));
		final ObjectHolder<List<UIActionForCheckboxForRole>> actionsHolder = new ObjectHolder<List<UIActionForCheckboxForRole>>();
		final InitializingModel<Collection<UIActionForCheckboxForRole>> actionsCheckGroupModel = new InitializingModel<Collection<UIActionForCheckboxForRole>>() {

			@Override
			protected Collection<UIActionForCheckboxForRole> getInitialValue() {
				final Collection<UIActionForCheckboxForRole> checkedActions = new HashSet<UIActionForCheckboxForRole>();
				for (UIActionForCheckboxForRole action : actionsHolder
						.getObject()) {
					if (action.isMapped()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}

		};
		final IDataProvider<UIActionForCheckboxForRole> actionsProvider = new BaseDataProvider<UIActionForCheckboxForRole>() {

			public Iterator<? extends UIActionForCheckboxForRole> iterator(
					int first, int count) {
				List<UIActionForCheckboxForRole> allRoleActions = roleService
						.getAllRoleActions(first, count,
								DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
								DaoConstants.ASC, roleId, filters
										.getActionNameSubstring());
				actionsHolder.setObject(allRoleActions);
				actionsCheckGroupModel.clearInitialized();
				return allRoleActions.iterator();
			}

			public int size() {
				return roleService.getAllRoleActionsCount(roleId, filters.getActionNameSubstring());
			}

		};
		final Form<Void> form = new Form<Void>("form") {
			public void onSubmit() {
				doSubmit(roleId, actionsHolder.getObject(), actionsCheckGroupModel.getObject());
			}
		};
		add(form);
		final CheckGroup<UIActionForCheckboxForRole> group = new CheckGroup<UIActionForCheckboxForRole>("group", actionsCheckGroupModel);
		form.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		DataView<UIActionForCheckboxForRole> actionsDataView = new PagingDataView<UIActionForCheckboxForRole>("actions", actionsProvider) {
			@Override
			protected void populateItem(Item<UIActionForCheckboxForRole> item) {
				UIActionForCheckboxForRole action = item.getModelObject();
				item.add(new Check<UIActionForCheckboxForRole>("mapped", item.getModel(), group));
				item.add(new Label("subsystem-name", action.getSubsystemName()));
				item.add(new Label("role-name", action.getRoleName()));
				item.add(new Label("action-name", action.getActionName()));
			}
		};
		group.add(actionsDataView);

		form.add(new PagingNavigator("paging-navigator", actionsDataView));
		
		form.add(new SubmitLink("save-actions-link"));
		form.add(new BookmarkablePageLink<Page>("cancel", ListRolesPage.class));
		
	}
	protected void doSubmit(long roleId,
			List<UIActionForCheckboxForRole> allActions,
			Collection<UIActionForCheckboxForRole> checkedActions) {
		List<Long> idsToAdd = new ArrayList<Long>();
		List<Long> idsToRemove = new ArrayList<Long>();
		for (UIActionForCheckboxForRole action : allActions) {
			if (checkedActions.contains(action)) {
				idsToAdd.add(action.getActionId());
			} else {
				idsToRemove.add(action.getActionId());
			}
		}
		
		roleService.changeRoleActions(roleId, idsToAdd, idsToRemove);
		
		info("Role changed");
		getRequestCycle().setResponsePage(ListRolesPage.class);
		getRequestCycle().setRedirect(true);
	}
	@Override
	protected String getTitle() {
		return "Role actions";
	}

	private static class Filters implements Serializable {
		private String actionNameSubstring;

		public String getActionNameSubstring() {
			return actionNameSubstring;
		}

		@SuppressWarnings("unused")
		public void setActionNameSubstring(String actionNameSubstring) {
			this.actionNameSubstring = actionNameSubstring;
		}
	}
}
