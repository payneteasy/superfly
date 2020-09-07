package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private SubsystemService subsystemService;
    private boolean isVisible = true;

    public ChangeUserRolesPage(PageParameters params) {
        super(ListUsersPage.class, params);

        final long userId = params.get("userId").toLong();
        final long subId = params.get("subId").toLong();

        UIUser user = userService.getUser(userId);
        UISubsystem subsystem = subsystemService.getSubsystem(subId);

        WebMarkupContainer container = new WebMarkupContainer("container");

        UIUserWithRolesAndActions user1 = userService.getUserRoleActions(userId, null, null, null);
        final List<UIRoleWithActions> roleWithAction = user1.getRoles();
        final SortRoleOfSubsystem sort = new SortRoleOfSubsystem();
        sort.setRoleWithAction(roleWithAction);
        List<UIRoleForCheckbox> rolesAll = userService.getUnmappedUserRoles(userId, subId, 0, Integer.MAX_VALUE);

        List<UIRoleWithActions> rolesChecked = sort.getRoles(subsystem.getName());
        List<UIRoleForCheckbox> selectedRole = new ArrayList<UIRoleForCheckbox>();
        List<UIRoleForCheckbox> notSelectedRole = new ArrayList<UIRoleForCheckbox>();
        for (UIRoleForCheckbox uic : rolesAll) {
            for (UIRoleWithActions uirwa : rolesChecked) {
                if (uic.getId() == uirwa.getId()) {
                    selectedRole.add(uic);
                }
            }
        }
        for (UIRoleForCheckbox uir : rolesAll) {
            if (!selectedRole.contains(uir)) {
                notSelectedRole.add(uir);
            }
        }
        if(notSelectedRole.isEmpty()){
            isVisible = false;
        }
        container.setVisible(isVisible);
        add(container);

        Form<Void> form = new Form<Void>("form");
        container.add(form);
        final ListRole listRole = new ListRole();

        form.add(new LabelValueRow<String>("user-name", new Model<String>(user.getUsername()), "user.name"));
        form.add(new LabelValueRow<String>("sub-name", new Model<String>(subsystem.getName()), "user.subsystem"));

        LabelDropDownChoiceRow<UIRoleForCheckbox> role = new LabelDropDownChoiceRow<UIRoleForCheckbox>("role", listRole, "user.role", notSelectedRole, new RoleChoiceRenderer());
        role.getDropDownChoice().setRequired(true);
        form.add(role);
        form.add(new Button("add-role") {

            @Override
            public void onSubmit() {
                List<Long> rolesId = new ArrayList<Long>();
                rolesId.add(listRole.getRole().getId());
                RoutineResult result = userService.changeUserRoles(userId, rolesId, null, null);
                if (result.isOk()) {
                    info("Roles changed; please be aware that some sessions could be invalidated");
                } else {
                    error("Error while changing user roles: " + result.getErrorMessage());
                }
                PageParameters parameters = new PageParameters();
                parameters.set("userId", String.valueOf(userId));
                setResponsePage(UserDetailsPage.class, parameters);
            }

        });
        final PageParameters parameters = new PageParameters();
        parameters.set("userId", String.valueOf(userId));
        form.add(new BookmarkablePageLink<Page>("cancel", UserDetailsPage.class, parameters));

        WebMarkupContainer noMoreRole = new WebMarkupContainer("no-more-roles-container");
        noMoreRole.setVisible(!isVisible);
        noMoreRole.add(new BookmarkablePageLink<Page>("back", UserDetailsPage.class, parameters));
        add(noMoreRole);
    }

    @Override
    protected String getTitle() {
        return "add roles";
    }

    @SuppressWarnings("unused")
    private class ListRole implements Serializable {
        private UIRoleForCheckbox role;

        public UIRoleForCheckbox getRole() {
            return role;
        }

        public void setRole(UIRoleForCheckbox role) {
            this.role = role;
        }

    }
}
