package com.payneteasy.superfly.web.wicket.page.role;

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

import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.page.BasePage;
@Secured("ROLE_ADMIN")
public class ChangeRoleGroupsPage extends BasePage {
	@SpringBean
	private RoleService roleService;
	public ChangeRoleGroupsPage(PageParameters parameters) {
         super(parameters);
         final long roleId = parameters.getAsLong("id", -1);
         final List<UIGroupForCheckbox> listGroups = roleService.getAllRoleGroups(roleId);
         
         Form<UIGroupForCheckbox> form = new Form<UIGroupForCheckbox>("form"){

			@Override
			protected void onSubmit() {
				doSubmit(roleId, listGroups);
			}
        	 
         };
         add(form);
         ListView<UIGroupForCheckbox> listView = new ListView<UIGroupForCheckbox>("list-groups",listGroups){

			@Override
			protected void populateItem(ListItem<UIGroupForCheckbox> item) {
				UIGroupForCheckbox uigroup = item.getModelObject();
				item.add(new CheckBox("mapped", new PropertyModel<Boolean>(uigroup, "mapped")));
				item.add(new Label("group-name",uigroup.getGroupName()));
				item.add(new Label("subsystem-name",uigroup.getSubsystemName()));
			}
        	 
         };
         form.add(listView);
         form.add(new BookmarkablePageLink<Page>("cancel", ListRolesPage.class));
         
	}
	protected void doSubmit(long roleId, List<UIGroupForCheckbox> groups) {
 		List<Long> idsToAdd = new ArrayList<Long>();
 		List<Long> idsToRemove = new ArrayList<Long>();
 		for (UIGroupForCheckbox group : groups) {
 			if (group.isMapped()) {
 				idsToAdd.add(group.getGroupId());
 			} else {
 				idsToRemove.add(group.getGroupId());
 			}
 		}
 		
 		roleService.changeRoleGroups(roleId, idsToAdd, idsToRemove);
 		getRequestCycle().setResponsePage(ListRolesPage.class);
 		getRequestCycle().setRedirect(true);
 	}
	@Override
	protected String getTitle() {
		return "Role Groups";
	}

}
