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
import com.payneteasy.superfly.service.mapping.MappingService;
import com.payneteasy.superfly.web.wicket.component.mapping.MappingPanel;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;
import com.payneteasy.superfly.web.wicket.page.group.ChangeGroupActionsPage;

/**
 * Used to change actions assigned to a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ChangeUserGrantActionsPage extends BasePage {

	@SpringBean
	private UserService userService;
	@SpringBean
	private SubsystemService subsystemService;
	@SpringBean
	private ActionService actionService;
	@SpringBean
	private RoleService roleService;

	public ChangeUserGrantActionsPage(final PageParameters params) {
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
		
		add(new MappingPanel("mapping-panel", userId){

			@Override
			protected List<? extends MappingService> getMappedItems(String searchLabel) {
				return userService.getMappedUserActions(userId, subId, searchLabel, 0, Integer.MAX_VALUE);
			}

			@Override
			protected List<? extends MappingService> getUnMappedItems(String searchLabel) {
				return userService.getUnmappedUserActions(userId, subId, searchLabel, 0, Integer.MAX_VALUE);
			}

			@Override
			protected void mappingProcess(long entityId, List<Long> mappedId, List<Long> unmappedId) {
				userService.changeUserRoleActions(userId, mappedId, unmappedId);
				setResponsePage(ChangeUserGrantActionsPage.class, params);
			}

			@Override
			protected boolean isVisibleSearchePanel() {
				return true;
			}

			@Override
			protected String getHeaderItemName() {
				return "Actions";
			}
			
		});
		
		add(new BookmarkablePageLink<Page>("back",UserDetailsPage.class,params));
	}


	@Override
	protected String getTitle() {
		return "User change grant actions";
	}
}
