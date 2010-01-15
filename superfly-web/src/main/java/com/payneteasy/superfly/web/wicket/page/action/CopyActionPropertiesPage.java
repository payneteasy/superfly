package com.payneteasy.superfly.web.wicket.page.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

@Secured("ROLE_ADMIN")
public class CopyActionPropertiesPage extends BasePage {
	@SpringBean
	private ActionService actionService;

	public CopyActionPropertiesPage(final PageParameters parameters) {
		super(parameters);
		final long actionId = parameters.getAsLong("id", -1);

		final ActionFilter actionFilter = new ActionFilter();
		Form<ActionFilter> filtersForm = new Form<ActionFilter>("filters-form");
		add(filtersForm);
		final AutoCompleteTextField<String> autoTextNameAction = new AutoCompleteTextField<String>(
				"auto", new Model("")) {

			@Override
			protected Iterator getChoices(String input) {
				if (Strings.isEmpty(input)) {
					return Collections.EMPTY_LIST.iterator();
				}
				List choices = new ArrayList(20);
				List<UIActionForFilter> action = actionService
						.getActionForFilter();
				for (UIActionForFilter uia : action) {
					final String name = uia.getActionName();
					if (name.toUpperCase().startsWith(input.toUpperCase())) {
						choices.add(name);

						if (choices.size() == 20) {
							break;
						}
					}
				}
				return choices.iterator();
			}
		};
		filtersForm.add(autoTextNameAction);

		UIAction action = actionService.getAction(actionId);
		final long subId = action.getSubsystemId();
		filtersForm.add(new Label("name-action", action == null ? null : action
				.getActionName()));
		filtersForm.add(new Label("name-description", action == null ? null
				: action.getActionDescription()));
		filtersForm.add(new Label("subname-action", action == null ? null
				: action.getSubsystemName()));
		filtersForm.add(new CheckBox("selected", new PropertyModel<Boolean>(
				actionFilter, "selected")));

		String[] fieldName = { "actionId", "actionName", "actionDescription",
				"subsystemName" };
		SortableDataProvider<UIActionForList> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(
				fieldName) {

			public Iterator<? extends UIActionForList> iterator(int first,
					int count) {
				String actionForFilter = autoTextNameAction.getModelObject();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subId);
				List<UIActionForList> actions = actionService.getActions(first,
						count, getSortFieldIndex(), isAscending(),
						actionForFilter == null ? null : actionForFilter, null,
						subsystemId);
				return actions.iterator();
			}

			public int size() {
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subId);
				return actionService.getActionCount(null, null, subsystemId);

			}

		};
		final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>(
				"actionList", actionDataProvider) {

			@Override
			protected void populateItem(Item<UIActionForList> item) {
				final UIActionForList action = item.getModelObject();
				Link<Void> selectActionForCopy = new Link<Void>("select-action") {

					@Override
					public void onClick() {
						actionFilter.setActionId(action.getId());
						info(action.getName()+" selected");

					}

				};
				selectActionForCopy.add(new Label("action-name", action
						.getName()));
				item.add(selectActionForCopy);
				item.add(new Label("action-description", action
						.getDescroption()));
				item
						.add(new Label("subsystem-name", action
								.getSubsystemName()));
			}

		};
		filtersForm.add(actionDataView);
		filtersForm.add(new OrderByLink("order-by-actionName", "actionName",
				actionDataProvider));
		filtersForm.add(new OrderByLink("order-by-actionDescription",
				"actionDescription", actionDataProvider));
		filtersForm.add(new OrderByLink("order-by-subsystemName",
				"subsystemName", actionDataProvider));
		filtersForm
				.add(new PagingNavigator("paging-navigator", actionDataView));
		filtersForm.add(new Button("cancel") {

			@Override
			public void onSubmit() {
				setResponsePage(ListActionsPage.class);
			}

		});
		filtersForm.add(new Button("copy") {

			@Override
			public void onSubmit() {
				actionService.copyActionProperties(actionFilter.getActionId(),
						actionId, actionFilter.isSelected());
				setResponsePage(ListActionsPage.class);
			}

		});

	}

	@SuppressWarnings("unused")
	private class ActionFilter implements Serializable {
		private UIActionForFilter actionForFilter;
		private long actionId;
		private boolean selected;

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public long getActionId() {
			return actionId;
		}

		public void setActionId(long actionId) {
			this.actionId = actionId;
		}

		public UIActionForFilter getActionForFilter() {
			return actionForFilter;
		}

		public void setActionForFilter(UIActionForFilter actionForFilter) {
			this.actionForFilter = actionForFilter;
		}
	}

	@Override
	protected String getTitle() {
		return "Copy action";
	}

}
