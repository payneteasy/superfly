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
import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;
@Secured("ROLE_ADMIN")
public class ChangeRoleGroupsPage extends BasePage {
	@SpringBean
	private RoleService roleService;
	public ChangeRoleGroupsPage(PageParameters parameters) {
	  super(ListRolesPage.class, parameters);
	  
      final long roleId = parameters.getAsLong("id");
      UIRole role = roleService.getRole(roleId);
      add(new Label("role-name",role.getRoleName()));
      //groups mapped
      List<UIGroupForCheckbox> roleGroupMapped = roleService.getMappedRoleGroups(0, Integer.MAX_VALUE, 1, true, roleId);
      final List<SelectObjectWrapper<UIGroupForCheckbox>> roleGroupMappedWrapper = new ArrayList<SelectObjectWrapper<UIGroupForCheckbox>>();
      for(UIGroupForCheckbox uig: roleGroupMapped){
    	  roleGroupMappedWrapper.add(new SelectObjectWrapper<UIGroupForCheckbox>(uig));
      }
      final InitializingModel<Collection<SelectObjectWrapper<UIGroupForCheckbox>>> groupsCheckActionsModelMap = new InitializingModel<Collection<SelectObjectWrapper<UIGroupForCheckbox>>>() {

			@Override
			protected Collection<SelectObjectWrapper<UIGroupForCheckbox>> getInitialValue() {
				final Collection<SelectObjectWrapper<UIGroupForCheckbox>> checkedActions = new HashSet<SelectObjectWrapper<UIGroupForCheckbox>>();
				for (SelectObjectWrapper<UIGroupForCheckbox> action : roleGroupMappedWrapper) {
					if (action.isSelected()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}

		};
		Form<Void> form = new Form("form");
		add(form);
		final CheckGroup<SelectObjectWrapper<UIGroupForCheckbox>> groupMap = new CheckGroup<SelectObjectWrapper<UIGroupForCheckbox>>(
				"group-map", groupsCheckActionsModelMap);
		form.add(groupMap);
		groupMap.add(new CheckGroupSelector("master-checkbox-map", groupMap));
		ListView<SelectObjectWrapper<UIGroupForCheckbox>> groupMapListView = new ListView<SelectObjectWrapper<UIGroupForCheckbox>>(
				"list-group-map", roleGroupMappedWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIGroupForCheckbox>> item) {
				SelectObjectWrapper<UIGroupForCheckbox> action = item
						.getModelObject();
				item.add(new Label("group-name", action.getObject()
						.getGroupName()));
				item.add(new Label("group-sub", action.getObject()
						.getSubsystemName()));
				item
						.add(new Check<SelectObjectWrapper<UIGroupForCheckbox>>(
								"selected", item.getModel()));
			}

		};
		groupMap.add(groupMapListView);
		form.add(new Button("group1-unmap"){

			@Override
			public void onSubmit() {
				Collection<SelectObjectWrapper<UIGroupForCheckbox>> checkedActions = groupsCheckActionsModelMap
				.getObject();
				List<Long> delIds = new ArrayList<Long>();
				List<Long> addIds = new ArrayList<Long>();
				for(SelectObjectWrapper<UIGroupForCheckbox> sow: checkedActions){
					if(sow.isSelected()){delIds.add(sow.getObject().getGroupId());}
					else{addIds.add(sow.getObject().getGroupId());}
				}
				RoutineResult result = roleService.changeRoleGroups(roleId, delIds, addIds);
				if (result.isOk()) {
					info("Groups changed; please be aware that some sessions could be invalidated");
				} else {
					error("Error while changing user actions: "
							+ result.getErrorMessage());
				}
				PageParameters parameters = new PageParameters();
				parameters.add("id", String.valueOf(roleId));
				setResponsePage(ChangeRoleGroupsPage.class, parameters);
			}
			
		});
		//groups unmap
		
		List<UIGroupForCheckbox> roleGroupUnMapped = roleService.getUnMappedRoleGroups(0, Integer.MAX_VALUE, 1, true, roleId);
	      final List<SelectObjectWrapper<UIGroupForCheckbox>> roleGroupUnMappedWrapper = new ArrayList<SelectObjectWrapper<UIGroupForCheckbox>>();
	      for(UIGroupForCheckbox uig: roleGroupUnMapped){
	    	  roleGroupUnMappedWrapper.add(new SelectObjectWrapper<UIGroupForCheckbox>(uig));
	      }
	      final InitializingModel<Collection<SelectObjectWrapper<UIGroupForCheckbox>>> groupsCheckActionsModelUnMap = new InitializingModel<Collection<SelectObjectWrapper<UIGroupForCheckbox>>>() {

				@Override
				protected Collection<SelectObjectWrapper<UIGroupForCheckbox>> getInitialValue() {
					final Collection<SelectObjectWrapper<UIGroupForCheckbox>> checkedActions = new HashSet<SelectObjectWrapper<UIGroupForCheckbox>>();
					for (SelectObjectWrapper<UIGroupForCheckbox> action : roleGroupUnMappedWrapper) {
						if (action.isSelected()) {
							checkedActions.add(action);
						}
					}
					return checkedActions;
				}

			};
			final CheckGroup<SelectObjectWrapper<UIGroupForCheckbox>> groupUnMap = new CheckGroup<SelectObjectWrapper<UIGroupForCheckbox>>(
					"group-unmap", groupsCheckActionsModelUnMap);
			form.add(groupUnMap);
			groupUnMap.add(new CheckGroupSelector("master-checkbox-unmap", groupUnMap));
			ListView<SelectObjectWrapper<UIGroupForCheckbox>> groupUnMapListView = new ListView<SelectObjectWrapper<UIGroupForCheckbox>>(
					"list-group-unmap", roleGroupUnMappedWrapper) {

				@Override
				protected void populateItem(
						ListItem<SelectObjectWrapper<UIGroupForCheckbox>> item) {
					SelectObjectWrapper<UIGroupForCheckbox> action = item
							.getModelObject();
					item.add(new Label("group-name", action.getObject()
							.getGroupName()));
					item.add(new Label("group-sub", action.getObject()
							.getSubsystemName()));
					item
							.add(new Check<SelectObjectWrapper<UIGroupForCheckbox>>(
									"selected", item.getModel()));
				}

			};
			groupUnMap.add(groupUnMapListView);
			form.add(new Button("group1-map"){

				@Override
				public void onSubmit() {
					Collection<SelectObjectWrapper<UIGroupForCheckbox>> checkedActions = groupsCheckActionsModelUnMap
					.getObject();
					List<Long> delIds = new ArrayList<Long>();
					List<Long> addIds = new ArrayList<Long>();
					for(SelectObjectWrapper<UIGroupForCheckbox> sow: checkedActions){
						if(sow.isSelected()){delIds.add(sow.getObject().getGroupId());}
						else{addIds.add(sow.getObject().getGroupId());}
					}
					RoutineResult result = roleService.changeRoleGroups(roleId, addIds, delIds);
					if (result.isOk()) {
						info("Groups changed; please be aware that some sessions could be invalidated");
					} else {
						error("Error while changing user actions: "
								+ result.getErrorMessage());
					}
					PageParameters parameters = new PageParameters();
					parameters.add("id", String.valueOf(roleId));
					setResponsePage(ChangeRoleGroupsPage.class, parameters);
				}
				
			});
			add(new BookmarkablePageLink<Page>("back", ListRolesPage.class,
					parameters));
	}

	@Override
	protected String getTitle() {
		return "Change group";
	}

}
