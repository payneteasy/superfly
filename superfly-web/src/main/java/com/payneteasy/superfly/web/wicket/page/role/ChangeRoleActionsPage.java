package com.payneteasy.superfly.web.wicket.page.role;

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
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

@Secured("ROLE_ADMIN")
public class ChangeRoleActionsPage extends BasePage {
	@SpringBean
	private RoleService roleService;

	public ChangeRoleActionsPage(PageParameters parameters) {
		final long roleId = parameters.getAsLong("id");
		UIRole role = roleService.getRole(roleId);
		add(new Label("role-name",role.getRoleName()));
		//actions map
		List<UIActionForCheckboxForRole> roleActionsMapped = roleService
				.getMappedRoleActions(0, Integer.MAX_VALUE, 5, true, roleId,
						null);
		final List<SelectObjectWrapper<UIActionForCheckboxForRole>> roleActionsMappedWrapper = new ArrayList<SelectObjectWrapper<UIActionForCheckboxForRole>>();
		for (UIActionForCheckboxForRole uia : roleActionsMapped) {
			roleActionsMappedWrapper
					.add(new SelectObjectWrapper<UIActionForCheckboxForRole>(
							uia));
		}
		final InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForRole>>> actionsCheckActionsModelMap = new InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForRole>>>() {

			@Override
			protected Collection<SelectObjectWrapper<UIActionForCheckboxForRole>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIActionForCheckboxForRole>> checkedActions = new HashSet<SelectObjectWrapper<UIActionForCheckboxForRole>>();
				for (SelectObjectWrapper<UIActionForCheckboxForRole> action : roleActionsMappedWrapper) {
					if (action.isSelected()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}

		};
		Form<Void> form = new Form<Void>("form");
		add(form);
		final CheckGroup<SelectObjectWrapper<UIActionForCheckboxForRole>> groupMap = new CheckGroup<SelectObjectWrapper<UIActionForCheckboxForRole>>(
				"group-map", actionsCheckActionsModelMap);
		form.add(groupMap);
		groupMap.add(new CheckGroupSelector("master-checkbox-map", groupMap));
		ListView<SelectObjectWrapper<UIActionForCheckboxForRole>> actionMapListView = new ListView<SelectObjectWrapper<UIActionForCheckboxForRole>>(
				"list-action-map", roleActionsMappedWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForCheckboxForRole>> item) {
				SelectObjectWrapper<UIActionForCheckboxForRole> action = item
						.getModelObject();
				item.add(new Label("action-name", action.getObject()
						.getActionName()));
				item.add(new Label("action-sub", action.getObject()
						.getSubsystemName()));
				item
						.add(new Check<SelectObjectWrapper<UIActionForCheckboxForRole>>(
								"selected", item.getModel()));
			}

		};
		groupMap.add(actionMapListView);
		form.add(new Button("action-unmap") {
			
			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIActionForCheckboxForRole>> checkedActions = actionsCheckActionsModelMap
				.getObject();
				List<Long> delIds = new ArrayList<Long>();
				List<Long> addIds = new ArrayList<Long>();
				for(SelectObjectWrapper<UIActionForCheckboxForRole> sow: checkedActions){
					if(sow.isSelected()){delIds.add(sow.getObject().getActionId());}
					else{addIds.add(sow.getObject().getActionId());}
				}
				RoutineResult result = roleService.changeRoleActions(roleId, delIds, addIds);
				if (result.isOk()) {
					info("Actions changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("id", String.valueOf(roleId));
				setResponsePage(ChangeRoleActionsPage.class, parameters);
			}
			
			
		});
		//unmap actions 
		List<UIActionForCheckboxForRole> roleActionsUnMapped = roleService
				.getUnMappedRoleActions(0, Integer.MAX_VALUE, 5, true, roleId, null);
		final List<SelectObjectWrapper<UIActionForCheckboxForRole>> roleActionsUnMappedWrapper = new ArrayList<SelectObjectWrapper<UIActionForCheckboxForRole>>();
		for(UIActionForCheckboxForRole uia: roleActionsUnMapped){
			roleActionsUnMappedWrapper.add(new SelectObjectWrapper<UIActionForCheckboxForRole>(uia));
		}
		final InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForRole>>> actionsCheckActionsModelUnMap = new InitializingModel<Collection<SelectObjectWrapper<UIActionForCheckboxForRole>>>() {

			@Override
			protected Collection<SelectObjectWrapper<UIActionForCheckboxForRole>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIActionForCheckboxForRole>> checkedActions = new HashSet<SelectObjectWrapper<UIActionForCheckboxForRole>>();
				for (SelectObjectWrapper<UIActionForCheckboxForRole> action : roleActionsUnMappedWrapper) {
					if (action.isSelected()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}

		};
		final CheckGroup<SelectObjectWrapper<UIActionForCheckboxForRole>> groupUnMap = new CheckGroup<SelectObjectWrapper<UIActionForCheckboxForRole>>(
				"group-unmap", actionsCheckActionsModelUnMap);
		form.add(groupUnMap);
		groupUnMap.add(new CheckGroupSelector("master-checkbox-unmap", groupUnMap));
		ListView<SelectObjectWrapper<UIActionForCheckboxForRole>> actionUnMapListView = new ListView<SelectObjectWrapper<UIActionForCheckboxForRole>>(
				"list-action-unmap", roleActionsUnMappedWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForCheckboxForRole>> item) {
				SelectObjectWrapper<UIActionForCheckboxForRole> action = item
						.getModelObject();
				item.add(new Label("action-name", action.getObject()
						.getActionName()));
				item.add(new Label("action-sub", action.getObject()
						.getSubsystemName()));
				item
						.add(new Check<SelectObjectWrapper<UIActionForCheckboxForRole>>(
								"selected", item.getModel()));
			}

		};
		groupUnMap.add(actionUnMapListView);
		form.add(new Button("action-map"){

			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIActionForCheckboxForRole>> checkedActions = actionsCheckActionsModelUnMap
				.getObject();
				List<Long> delIds = new ArrayList<Long>();
				List<Long> addIds = new ArrayList<Long>();
				for(SelectObjectWrapper<UIActionForCheckboxForRole> sow: checkedActions){
					if(sow.isSelected()){delIds.add(sow.getObject().getActionId());}
					else{addIds.add(sow.getObject().getActionId());}
				}
				RoutineResult result = roleService.changeRoleActions(roleId, addIds, delIds);
				if (result.isOk()) {
					info("Actions changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("id", String.valueOf(roleId));
				setResponsePage(ChangeRoleActionsPage.class, parameters);
			}
			
		});
		add(new BookmarkablePageLink<Page>("back", ListRolesPage.class,
				parameters));
	}
	@Override
	protected String getTitle() {
		return "change actions";
	}

}
