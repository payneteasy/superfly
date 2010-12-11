package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

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
	@SpringBean
	private ActionService actionService;
	@SpringBean
	private RoleService roleService;

	public ChangeUserActionsPage(PageParameters params) {
		super(ListUsersPage.class, params);

		final long userId = params.getAsLong("userId");
		final long subId = params.getAsLong("subId");
		final long roleId = params.getAsLong("roleId");

		UIUser user = userService.getUser(userId);
		UISubsystem subsystem = subsystemService.getSubsystem(subId);
		UIRole role = roleService.getRole(roleId);
		add(new Label("user-name", user.getUsername()));
		add(new Label("sub-name", subsystem.getName()));
		add(new Label("role-name",role.getRoleName()));
		final Filters filters = new Filters(); 
		// LIst mapp action
		final List<SelectObjectWrapper<UIActionForCheckboxForUser>> actionsUnmapWrap = new ArrayList<SelectObjectWrapper<UIActionForCheckboxForUser>>();
		List<UIActionForCheckboxForUser> actionsForChek= userService
		.getMappedUserActions(userId, subId, null, 0, Integer.MAX_VALUE);
		for(UIActionForCheckboxForUser uia: actionsForChek){
			actionsUnmapWrap.add(new SelectObjectWrapper<UIActionForCheckboxForUser>(uia));
		}
		final InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForUser>>> actionsCheckGroupModelMap = new InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForUser>>>() {

			@Override
			protected Collection<SelectObjectWrapper<UIActionForCheckboxForUser>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIActionForCheckboxForUser>> checkedSubsystems = new HashSet<SelectObjectWrapper<UIActionForCheckboxForUser>>();
				for (SelectObjectWrapper<UIActionForCheckboxForUser> subsystem : actionsUnmapWrap) {
					if (subsystem.isSelected()) {
						checkedSubsystems.add(subsystem);
					}
				}
				return checkedSubsystems;
			}

		};
		final Form<Void> formMap = new Form<Void>("form");
		add(formMap);
		final CheckGroup<SelectObjectWrapper<UIActionForCheckboxForUser>> groupMap = new CheckGroup<SelectObjectWrapper<UIActionForCheckboxForUser>>(
				"group-map", actionsCheckGroupModelMap);
		formMap.add(groupMap);
		groupMap.add(new CheckGroupSelector("master-checkbox-map", groupMap));
		ListView<SelectObjectWrapper<UIActionForCheckboxForUser>> actionsDataViewMap = new ListView<SelectObjectWrapper<UIActionForCheckboxForUser>>(
				"actions-map", actionsUnmapWrap) {
			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForCheckboxForUser>> item) {
				SelectObjectWrapper<UIActionForCheckboxForUser> action = item.getModelObject();
				item.add(new Check<SelectObjectWrapper<UIActionForCheckboxForUser>>("selected",
						item.getModel()));
				UIAction act = actionService.getAction(action.getObject().getActionId());
				item.add(new Label("action-name", action.getObject().getActionName()));
				item.add(new Label("action-description",act.getActionDescription()));
				
			}
		};
		groupMap.add(actionsDataViewMap);
		formMap.add(new Button("unmap-action"){

			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIActionForCheckboxForUser>> checkedActions = actionsCheckGroupModelMap.getObject();
				Set<Long> delIds = new HashSet<Long>();
				Set<Long> addIds = new HashSet<Long>();
				for (SelectObjectWrapper<UIActionForCheckboxForUser> action : checkedActions) {
					if (action.isSelected()) {
						delIds.add(action.getObject().getRoleActionId());
					}else{addIds.add(action.getObject().getRoleActionId());}
				}
				RoutineResult result = userService.changeUserRoleActions(userId,
						delIds, addIds);
				if (result.isOk()) {
					info("Actions changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("userId", String.valueOf(userId));
				parameters.add("subId", String.valueOf(subId));
				parameters.add("roleId", String.valueOf(roleId));
				setResponsePage(ChangeUserActionsPage.class, parameters);
				
			}
			
		});		
	
		// list unmap action
		final List<SelectObjectWrapper<UIActionForCheckboxForUser>> actionsMapWrap = new ArrayList<SelectObjectWrapper<UIActionForCheckboxForUser>>();
		final List<UIActionForCheckboxForUser> actionMap = userService.getUnmappedUserActions(userId, subId, null, 0, Integer.MAX_VALUE);
		for(UIActionForCheckboxForUser uia: actionMap){
			actionsMapWrap.add(new SelectObjectWrapper<UIActionForCheckboxForUser>(uia));
		}

		final InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForUser>>> actionsCheckGroupModel = new InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForUser>>>() {
			@Override
			protected Collection<SelectObjectWrapper<UIActionForCheckboxForUser>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIActionForCheckboxForUser>> checkedActions = new HashSet<SelectObjectWrapper<UIActionForCheckboxForUser>>();
				for(SelectObjectWrapper<UIActionForCheckboxForUser> sow: actionsMapWrap){
					if(sow.isSelected()){
						checkedActions.add(sow);
					}
				}
				return checkedActions;
			}
		};
		
		final CheckGroup<SelectObjectWrapper<UIActionForCheckboxForUser>> group = new CheckGroup<SelectObjectWrapper<UIActionForCheckboxForUser>>(
				"group", actionsCheckGroupModel);
		formMap.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		ListView<SelectObjectWrapper<UIActionForCheckboxForUser>> actionsDataView = new ListView<SelectObjectWrapper<UIActionForCheckboxForUser>>(
				"actions", actionsMapWrap) {
			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForCheckboxForUser>> item) {
				SelectObjectWrapper<UIActionForCheckboxForUser> action = item.getModelObject();
				item.add(new Check<SelectObjectWrapper<UIActionForCheckboxForUser>>("selected",
						item.getModel()));
				UIAction act = actionService.getAction(action.getObject().getActionId());
				item.add(new Label("action-name", action.getObject().getActionName()));
				item.add(new Label("action-description",act.getActionDescription()));
				
			}
		};
		group.add(actionsDataView);

		PageParameters pageParams1 = new PageParameters();
		pageParams1.add("userId", String.valueOf(userId));
		

		formMap.add(new Button("map-action"){

			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIActionForCheckboxForUser>> checkedActions = actionsCheckGroupModel.getObject();
				Set<Long> delIds = new HashSet<Long>();
				Set<Long> addIds = new HashSet<Long>();
				for (SelectObjectWrapper<UIActionForCheckboxForUser> action : checkedActions) {
					if (action.isSelected()) {
						delIds.add(action.getObject().getRoleActionId());
					}else{addIds.add(action.getObject().getRoleActionId());}
				}
				RoutineResult result = userService.changeUserRoleActions(userId,
						addIds, delIds);
				if (result.isOk()) {
					info("Actions changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("userId", String.valueOf(userId));
				parameters.add("subId", String.valueOf(subId));
				parameters.add("roleId", String.valueOf(roleId));
				setResponsePage(ChangeUserActionsPage.class, parameters);
				
			}
			
		});
		
		add(new BookmarkablePageLink<Page>("back",UserDetailsPage.class,pageParams1));
	}


	@Override
	protected String getTitle() {
		return "User actions";
	}

	@SuppressWarnings("unused")
	private static class Filters implements Serializable {
		

		private boolean unmappedOnly = true;
		private boolean mappedOnly=true;

		
		public boolean isMappedOnly() {
			return mappedOnly;
		}

		public void setMappedOnly(boolean mappedOnly) {
			this.mappedOnly = mappedOnly;
		}

		public boolean isUnmappedOnly() {
			return unmappedOnly;
		}

		public void setUnmappedOnly(boolean unmappedOnly) {
			this.unmappedOnly = unmappedOnly;
		}
	}
}
