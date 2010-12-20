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
import com.payneteasy.superfly.service.mapping.MappingService;
import com.payneteasy.superfly.web.wicket.component.mapping.MappingPanel;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

@Secured("ROLE_ADMIN")
public class ChangeRoleGroupsPage extends BasePage {
	@SpringBean
	private RoleService roleService;

	public ChangeRoleGroupsPage(final PageParameters parameters) {
		super(ListRolesPage.class, parameters);

		final long roleId = parameters.getAsLong("id");
		UIRole role = roleService.getRole(roleId);
		add(new Label("role-name", role.getRoleName()));
        add(new MappingPanel("mapping-panel",roleId){

			@Override
			protected List<? extends MappingService> getMappedItems(String searchLabel) {
				return roleService.getMappedRoleGroups(0, Integer.MAX_VALUE, 5, true, roleId);
			}

			@Override
			protected List<? extends MappingService> getUnMappedItems(String searchLabel) {
				return roleService.getUnMappedRoleGroups(0, Integer.MAX_VALUE, 5, true, roleId);
			}

			@Override
			protected void mappingProcess(long entityId, List<Long> mappedId, List<Long> unmappedId) {
				roleService.changeRoleGroups(roleId, mappedId, unmappedId);
				setResponsePage(ChangeRoleGroupsPage.class, parameters);
			}

			@Override
			protected boolean isVisibleSearchePanel() {
				return false;
			}

			@Override
			protected String getHeaderItemName() {
				return "Groups";
			}
        	
        });
		add(new BookmarkablePageLink<Page>("back", ListRolesPage.class, parameters));
	}

	@Override
	protected String getTitle() {
		return "Change group";
	}

}
