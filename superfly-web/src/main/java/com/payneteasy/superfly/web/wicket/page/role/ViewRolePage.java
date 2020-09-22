package com.payneteasy.superfly.web.wicket.page.role;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRoleForView;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@Secured("ROLE_ADMIN")
public class ViewRolePage extends BasePage {
    @SpringBean
    private RoleService roleService;

    @Override
    protected String getTitle() {
        return "Role details";
    }

    public ViewRolePage(PageParameters param) {
        super(ListRolesPage.class, param);

        final Long roleId = param.get("roleid").toLong();

        //BACK
        add(new Link<Void>("btn-back") {
            @Override
            public void onClick() {
                setResponsePage(ListRolesPage.class);
            }
        });

        //ROLE PROPERTIES
        final UIRoleForView curRole = roleService.getRole(roleId);

        add(new Label("roleName", curRole.getRoleName()));
        add(new Label("principalName", curRole.getPrincipalName()));
        add(new Label("roleSubsystem", curRole.getSubsystemName()));

        PageParameters pageParameters = PageParametersBuilder.fromPair("id", roleId);
        add(new BookmarkablePageLink<Page>("role-groups", ChangeRoleGroupsPage.class, pageParameters));
        add(new BookmarkablePageLink<Page>("role-actions", ChangeRoleActionsPage.class, pageParameters));

//        GRANT GROUPS
        add(new ListView<UIGroupForCheckbox>("grant-groups", roleService.getMappedRoleGroups(0, Integer.MAX_VALUE, 1, true, roleId)) {
            @Override
            protected void populateItem(ListItem<UIGroupForCheckbox> item) {
                UIGroupForCheckbox group = item.getModelObject();
                item.add(new Label("group-id",String.valueOf(group.getGroupId())));
                item.add(new Label("group-name",group.getGroupName()));
            }
        });

//        GRANT ACTIONS
        add(new ListView<UIActionForCheckboxForRole>("grant-actions",roleService.getMappedRoleActions(0, Integer.MAX_VALUE, 1, true, roleId, null)) {
            @Override
            protected void populateItem(ListItem<UIActionForCheckboxForRole> item) {
                final UIActionForCheckboxForRole action = item.getModelObject();
                item.add(new Label("action-id",String.valueOf(action.getActionId())));
                item.add(new Label("action-name",action.getActionName()));
            }
        });

    }


    @SuppressWarnings("unused")
    private static class Filter implements Serializable {
        private String actionNameSubstring;

        public String getActionNameSubstring() {
            return actionNameSubstring;
        }

        public void setActionNameSubstring(String actionNameSubstring) {
            this.actionNameSubstring = actionNameSubstring;
        }
    }
}
