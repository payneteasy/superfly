package com.payneteasy.superfly.web.wicket.page.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForGroup;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

@Secured("ROLE_ADMIN")
public class ChangeGroupActionsPage extends BasePage {
	@SpringBean
	private GroupService groupService;

	public ChangeGroupActionsPage(PageParameters parameters) {
		final long groupId = parameters.getAsLong("gid");
		UIGroup group = groupService.getGroupById(groupId);
		add(new Label("group-name", group.getName()));

		// list unmap action
		List<UIActionForCheckboxForGroup> unMappedActions = groupService
				.getAllGroupUnMappedActions(0, Integer.MAX_VALUE, 5, true,
						groupId, null);
		final List<SelectObjectWrapper<UIActionForCheckboxForGroup>> unMappedActionsWrapper = new ArrayList<SelectObjectWrapper<UIActionForCheckboxForGroup>>();
		for (UIActionForCheckboxForGroup uia : unMappedActions) {
			unMappedActionsWrapper
					.add(new SelectObjectWrapper<UIActionForCheckboxForGroup>(
							uia));
		}
		final InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>>> actionsCheckGroupModelUnMap = new InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>>>() {

			@Override
			protected Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>> checkedActions = new HashSet<SelectObjectWrapper<UIActionForCheckboxForGroup>>();
				for (SelectObjectWrapper<UIActionForCheckboxForGroup> action : unMappedActionsWrapper) {
					if (action.isSelected()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}

		};
		Form<Void> form = new Form<Void>("form");
		add(form);
		final CheckGroup<SelectObjectWrapper<UIActionForCheckboxForGroup>> groupUnMap = new CheckGroup<SelectObjectWrapper<UIActionForCheckboxForGroup>>(
				"group-unmap", actionsCheckGroupModelUnMap);
		form.add(groupUnMap);
		groupUnMap.add(new CheckGroupSelector("master-checkbox-unmap",
				groupUnMap));
		ListView<SelectObjectWrapper<UIActionForCheckboxForGroup>> actionUnMapListView = new ListView<SelectObjectWrapper<UIActionForCheckboxForGroup>>(
				"list-action-unmap", unMappedActionsWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForCheckboxForGroup>> item) {
				SelectObjectWrapper<UIActionForCheckboxForGroup> action = item
						.getModelObject();
				item.add(new Label("action-name", action.getObject()
						.getActionName()));
				item.add(new Label("action-sub", action.getObject()
						.getSubsystemName()));
				item
						.add(new Check<SelectObjectWrapper<UIActionForCheckboxForGroup>>(
								"selected", item.getModel()));
			}

		};
		groupUnMap.add(actionUnMapListView);
		form.add(new Button("action-map") {

			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>> checkedActions = actionsCheckGroupModelUnMap
						.getObject();
				List<Long> delIds = new ArrayList<Long>();
				List<Long> addIds = new ArrayList<Long>();
				for (SelectObjectWrapper<UIActionForCheckboxForGroup> action : checkedActions) {
					if (action.isSelected()) {
						delIds.add(action.getObject().getActionId());
					} else {
						addIds.add(action.getObject().getActionId());
					}
				}
				RoutineResult result = groupService.changeGroupActions(groupId,
						addIds, delIds);
				if (result.isOk()) {
					info("Actions changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("gid", String.valueOf(groupId));
				setResponsePage(ChangeGroupActionsPage.class, parameters);

			}

		});
		// list map action
		List<UIActionForCheckboxForGroup> mappedActions = groupService
				.getAllGroupMappedActions(0, Integer.MAX_VALUE, 5, true,
						groupId, null);
		final List<SelectObjectWrapper<UIActionForCheckboxForGroup>> mappedActionsWrapper = new ArrayList<SelectObjectWrapper<UIActionForCheckboxForGroup>>();
		for (UIActionForCheckboxForGroup uia : mappedActions) {
			mappedActionsWrapper
					.add(new SelectObjectWrapper<UIActionForCheckboxForGroup>(
							uia));
		}
		final InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>>> actionsCheckGroupModelMap = new InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>>>() {
			@Override
			protected Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>> checkedActions = new HashSet<SelectObjectWrapper<UIActionForCheckboxForGroup>>();
				for (SelectObjectWrapper<UIActionForCheckboxForGroup> sow : mappedActionsWrapper) {
					if (sow.isSelected()) {
						checkedActions.add(sow);
					}
				}
				return checkedActions;
			}
		};
		final CheckGroup<SelectObjectWrapper<UIActionForCheckboxForGroup>> groupMap = new CheckGroup<SelectObjectWrapper<UIActionForCheckboxForGroup>>(
				"group-map", actionsCheckGroupModelMap);
		form.add(groupMap);
		groupMap.add(new CheckGroupSelector("master-checkbox-map", groupMap));
		ListView<SelectObjectWrapper<UIActionForCheckboxForGroup>> actionsMapListView = new ListView<SelectObjectWrapper<UIActionForCheckboxForGroup>>(
				"list-action-map", mappedActionsWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForCheckboxForGroup>> item) {
				SelectObjectWrapper<UIActionForCheckboxForGroup> action = item.getModelObject();
				item.add(new Label("action-name", action.getObject().getActionName()));
				item.add(new Label("action-sub", action.getObject().getSubsystemName()));
				item.add(new Check<SelectObjectWrapper<UIActionForCheckboxForGroup>>(
						"selected", item.getModel()));

			}

		};
		groupMap.add(actionsMapListView);
		form.add(new Button("action-unmap") {

			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIActionForCheckboxForGroup>> checkedActions = actionsCheckGroupModelMap
						.getObject();
				List<Long> delIds = new ArrayList<Long>();
				List<Long> addIds = new ArrayList<Long>();
				for (SelectObjectWrapper<UIActionForCheckboxForGroup> action : checkedActions) {
					if (action.isSelected()) {
						delIds.add(action.getObject().getActionId());
					} else {
						addIds.add(action.getObject().getActionId());
					}
				}

				RoutineResult result = groupService.changeGroupActions(groupId,
						delIds, addIds);
				if (result.isOk()) {
					info("Actions changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("gid", String.valueOf(groupId));
				setResponsePage(ChangeGroupActionsPage.class, parameters);
			}

		});
		add(new BookmarkablePageLink<Page>("back", ListGroupsPage.class,
				parameters));
	}

	@Override
	protected String getTitle() {
		return "Change group actions";
	}
}
