package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.mapping.MappingPanel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

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
    private RoleService roleService;

    public ChangeUserGrantActionsPage(final PageParameters params) {
        super(ListUsersPage.class, params);

        final long userId = params.get("userId").toLong();
        final long subId = params.get("subId").toLong();
        final long roleId = params.get("roleId").toLong();

        UIUser user = userService.getUser(userId);
        UISubsystem subsystem = subsystemService.getSubsystem(subId);
        UIRole role = roleService.getRole(roleId);
        add(new Label("user-name", user.getUsername()));
        add(new Label("sub-name", subsystem.getName()));
        add(new Label("role-name",role.getRoleName()));

        add(new MappingPanel<UIActionForCheckboxForUser>("mapping-panel", userId){

            @Override
            protected List<UIActionForCheckboxForUser> getMappedItems(String searchLabel) {
                return userService.getMappedUserActions(userId, subId, roleId, searchLabel, 0, Integer.MAX_VALUE);
            }

            @Override
            protected List<UIActionForCheckboxForUser> getUnMappedItems(String searchLabel) {
                return userService.getUnmappedUserActions(userId, subId, roleId, searchLabel, 0, Integer.MAX_VALUE);
            }

            @Override
            protected void mappingProcess(long entityId, List<Long> mappedId, List<Long> unmappedId) {
                userService.changeUserRoleActions(userId, mappedId, unmappedId);
                setResponsePage(ChangeUserGrantActionsPage.class, params);
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
