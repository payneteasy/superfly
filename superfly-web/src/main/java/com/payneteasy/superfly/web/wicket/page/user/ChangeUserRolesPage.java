package com.payneteasy.superfly.web.wicket.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

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
	private RoleService roleService;

	public ChangeUserRolesPage(PageParameters params) {
		super(params);
		
		final long userId = params.getAsLong("userId", -1);
		
		final List<UIRoleForCheckbox> roles = roleService.getAllUserRoles(userId);
		
		Form<List<UIRoleForCheckbox>> form = new Form<List<UIRoleForCheckbox>>("form") {
			public void onSubmit() {
				doSubmit(userId, roles);
			}
		};
		add(form);
		ListView<UIRoleForCheckbox> rolesListView = new ListView<UIRoleForCheckbox>("roles", roles) {
			@Override
			protected void populateItem(ListItem<UIRoleForCheckbox> item) {
				UIRoleForCheckbox role = item.getModelObject();
				item.add(new CheckBox("mapped", new PropertyModel<Boolean>(role, "mapped")));
				item.add(new Label("subsystem-name", role.getSubsystemName()));
				item.add(new Label("role-name", role.getRoleName()));
			}
		};
		form.add(rolesListView);
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}

	protected void doSubmit(long userId, List<UIRoleForCheckbox> roles) {
		List<Long> idsToAdd = new ArrayList<Long>();
		List<Long> idsToRemove = new ArrayList<Long>();
		for (UIRoleForCheckbox role : roles) {
			if (role.isMapped()) {
				idsToAdd.add(role.getId());
			} else {
				idsToRemove.add(role.getId());
			}
		}
		
		userService.changeUserRoles(userId, idsToAdd, idsToRemove);
		
		info("Roles changed");
		getRequestCycle().setResponsePage(ListUsersPage.class);
		getRequestCycle().setRedirect(true);
	}
	
	@Override
	protected String getTitle() {
		return "User roles";
	}

}
