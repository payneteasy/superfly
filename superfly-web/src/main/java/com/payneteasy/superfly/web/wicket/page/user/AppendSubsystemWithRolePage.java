package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserAddSubsystemWithRole;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.RoleInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.component.SubsystemInCreateUserChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.util.*;

@Secured("ROLE_ADMIN")
public class AppendSubsystemWithRolePage extends BasePage {
    @SpringBean
    private UserService userService;
    @SpringBean
    private RoleService roleService;
    @SpringBean
    private SubsystemService subsystemService;

    public AppendSubsystemWithRolePage(PageParameters params) {
        super(ListUsersPage.class, params);
        final long userId = params.get("userId").toLong();

        WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        List<UISubsystemForList> listSub = subsystemService.getSubsystems();

        UIUserWithRolesAndActions user1 = userService.getUserRoleActions(userId, null, null, null);

        final List<UIRoleWithActions> roleWithAction = user1.getRoles();

        final SortRoleOfSubsystem sort = new SortRoleOfSubsystem();
        sort.setRoleWithAction(roleWithAction);
        List<String> oldSubName = sort.getSubsystemsName();
        List<UISubsystemForList> oldSub = new ArrayList<UISubsystemForList>();
        List<UISubsystemForList> newSub = new ArrayList<UISubsystemForList>();
        for (UISubsystemForList ui : listSub) {
            for (String sub : oldSubName) {
                if (sub.equals(ui.getName())) {
                    oldSub.add(ui);
                }
            }
        }
        for (UISubsystemForList old : listSub) {
            if (!oldSub.contains(old)) {
                newSub.add(old);
            }
        }

        boolean visible = true;
        if(newSub.isEmpty()){
            visible =false;
        }
        container.setVisible(visible);

        for (UISubsystemForList sub : newSub) {
            List<Long> listIdsub = new ArrayList<>();
            listIdsub.add(sub.getId());
            List<UIRoleForList> listRole = roleService.getRoles(0, Integer.MAX_VALUE, 1, true, null, listIdsub);
            modelsMap.put(sub, listRole);
        }

        // models for DropDrownChoice
        IModel<List<? extends UISubsystemForList>> subsystemsModel = new LoadableDetachableModel<>() {
            @Override
            public List<UISubsystemForList> load() {
                Set<UISubsystemForList>  keys = modelsMap.keySet();
                return new ArrayList<>(keys);
            }
        };
        final IModel<List<? extends UIRoleForList>> rolesModel = new LoadableDetachableModel<>() {
            @Override
            public List<UIRoleForList> load() {
                List<UIRoleForList> models = modelsMap.get(subsystem);
                if (models == null) {
                    models = Collections.emptyList();
                }
                return models;
            }

        };
        Form<UIUserAddSubsystemWithRole> form = new Form<UIUserAddSubsystemWithRole>("form");
        container.add(form);

//        USER INFO
        UIUser user = userService.getUser(userId);
        form.add(new LabelValueRow<String>("user-name", new Model<String>(user.getUsername()), "user.name"));

        LabelDropDownChoiceRow<UISubsystemForList> subsystemsRow = new LabelDropDownChoiceRow<UISubsystemForList>("subsystem", this, "user.create.choice-subsystem", subsystemsModel, new SubsystemInCreateUserChoiceRenderer());
        subsystemsRow.getDropDownChoice().setRequired(true);

        final LabelDropDownChoiceRow<UIRoleForList> rolesRow = new LabelDropDownChoiceRow<UIRoleForList>("role", new Model<UIRoleForList>(), "user.create.choice-roles", rolesModel, new RoleInCreateUserChoiceRender());
        rolesRow.getDropDownChoice().setRequired(true);
        rolesRow.setOutputMarkupId(true);

        form.add(subsystemsRow);
        form.add(rolesRow);
        subsystemsRow.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onchange"){

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                rolesModel.detach();
                target.add(rolesRow);
            }

        });
        form.add(new Button("add-sub") {

            @Override
            public void onSubmit() {
                UIRoleForList role = rolesRow.getDropDownChoice().getModelObject();
                userService.addSubsystemWithRole(userId, role.getId());
                PageParameters param = new PageParameters();
                param.set("userId", String.valueOf(userId));
                getRequestCycle().setResponsePage(UserDetailsPage.class, param);
            }

        });
        final PageParameters param = new PageParameters();
        param.set("userId", String.valueOf(userId));
        form.add(new BookmarkablePageLink<Page>("cancel", UserDetailsPage.class, param));

        WebMarkupContainer noMoreSubContainer = new WebMarkupContainer("no-more-sub-container");
        noMoreSubContainer.setVisible(!visible);
        noMoreSubContainer.add(new BookmarkablePageLink<Page>("back", UserDetailsPage.class, param));
        add(noMoreSubContainer);
    }

    @Override
    protected String getTitle() {
        return "Add subsystem with role";
    }

    private final Map<UISubsystemForList, List<UIRoleForList>> modelsMap = new HashMap<UISubsystemForList, List<UIRoleForList>>(); // map:company->model
    private UIRoleForList role;
    private UISubsystemForList subsystem;

    public UIRoleForList getRole() {
        return role;
    }

    public void setRole(UIRoleForList role) {
        this.role = role;
    }

    public UISubsystemForList getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(UISubsystemForList subsystem) {
        this.subsystem = subsystem;
    }
}
