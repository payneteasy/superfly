package com.payneteasy.superfly.web.wicket.page.role;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.model.StickyFilters;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import com.payneteasy.superfly.web.wicket.utils.WicketComponentHelper;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Secured("ROLE_ADMIN")
public class ListRolesPage extends BasePage {
    @SpringBean
    private RoleService roleService;
    @SpringBean
    private SubsystemService subsystemService;

    public ListRolesPage() {
        super(ListRolesPage.class);

        add(new EmptyPanel("confirmPanel"));

        final StickyFilters stickyFilters = getSession().getStickyFilters();
        Form<RoleFilter> filtersForm = new Form<RoleFilter>("filters-form");
        add(filtersForm);
        DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
                "subsystem-filter"
                , new PropertyModel<UISubsystemForFilter>(stickyFilters, "subsystem")
                , subsystemService.getSubsystemsForFilter()
                , new SubsystemChoiceRenderer()
        );
        subsystemDropdown.setNullValid(true);
        filtersForm.add(subsystemDropdown);

        final ObjectHolder<List<UIRoleForList>> rolesHolder = new ObjectHolder<List<UIRoleForList>>();
        final InitializingModel<Collection<UIRoleForList>> rolesCheckGroupModel = new InitializingModel<Collection<UIRoleForList>>() {
            @Override
            protected Collection<UIRoleForList> getInitialValue() {
                final Collection<UIRoleForList> checkedRoles = new HashSet<UIRoleForList>();
                for (UIRoleForList role : rolesHolder.getObject()) {
                    if (role.isSelected()) {
                        checkedRoles.add(role);
                    }
                }
                return checkedRoles;
            }

        };
        String[] fieldNames = { "roleId", "roleName", "principalName", "subsystemName" };
        SortableDataProvider<UIRoleForList, String> rolesDataProvider = new IndexedSortableDataProvider<UIRoleForList>(
                fieldNames) {

            public Iterator<? extends UIRoleForList> iterator(long first, long count) {
                UISubsystemForFilter subsystem = stickyFilters.getSubsystem();
                List<Long> subsystemId = new ArrayList<Long>();
                if (subsystem != null) {
                    subsystemId.add(subsystem.getId());
                }
                List<UIRoleForList> roles = roleService.getRoles(first, count,
                        getSortFieldIndex(), isAscending(), null,
                        subsystem == null ? null : subsystemId);
                rolesHolder.setObject(roles);
                rolesCheckGroupModel.clearInitialized();
                return roles.iterator();
            }

            public long size() {
                UISubsystemForFilter subsystem = stickyFilters.getSubsystem();
                List<Long> subsystemId = new ArrayList<Long>();
                if (subsystem != null) {
                    subsystemId.add(subsystem.getId());
                }
                return roleService.getRoleCount(null,
                        subsystem == null ? null : subsystemId);
            }

        };
        final Form<Void> form = new Form<Void>("form");
        add(form);
        final CheckGroup<UIRoleForList> group = new CheckGroup<UIRoleForList>("group", rolesCheckGroupModel);
        form.add(group);
        group.add(new CheckGroupSelector("master-checkbox", group));

        DataView<UIRoleForList> rolesDateView = new PagingDataView<UIRoleForList>("roleList", rolesDataProvider) {

            @Override
            protected void populateItem(Item<UIRoleForList> item) {
                final UIRoleForList role = item.getModelObject();

                PageParameters pageParams = new PageParameters();
                pageParams.set("roleid", String.valueOf(role.getId()));

                item.add(new Label("role-name", role.getName()));
                item.add(new Label("principal-name",role.getPrincipalName()));
                item.add(new Label("subsystem-name", role.getSubsystem()));
                item.add(new Check<UIRoleForList>("selected", item.getModel(),group));
                item.add(new BookmarkablePageLink<Page>("role-edit", EditRolePage.class, PageParametersBuilder.fromPair("id", role.getId())));
                item.add(new BookmarkablePageLink<Page>("role-groups", ChangeRoleGroupsPage.class, PageParametersBuilder.fromPair("id", role.getId())));
                item.add(new BookmarkablePageLink<Page>("role-actions", ChangeRoleActionsPage.class, PageParametersBuilder.fromPair("id", role.getId())));

                item.add(new SubmitLink("delete-role"){

                    @Override
                    public void onSubmit() {
                        this.getPage().get("confirmPanel").replaceWith(
                                new ConfirmPanel("confirmPanel",
                                        "You are about to delete "
                                                + " role - "+ role.getName()+" permanently?") {
                                    public void onConfirm() {
                                        RoutineResult result = roleService.deleteRole(role.getId());
                                        if (result.isOk()) {
                                            info("Deleted role; please be aware that some sessions could be invalidated");
                                        }
                                        this.getPage().setResponsePage(ListRolesPage.class);
                                    }
                                });
                    }
                });

                WicketComponentHelper.clickTableRow(item, ViewRolePage.class, pageParams, this);
            }

        };
        group.add(rolesDateView);
        group.add(new OrderByLink<>("order-by-roleName", "roleName", rolesDataProvider));
        group.add(new OrderByLink<>("order-by-principalName", "principalName", rolesDataProvider));
        group.add(new OrderByLink<>("order-by-subsystemName", "subsystemName", rolesDataProvider));

        group.add(new SuperflyPagingNavigator("paging-navigator", rolesDateView));

        form.add(new Button("delete-role"){

            @Override
            public void onSubmit() {
                final Collection<UIRoleForList> checkedRoles=rolesCheckGroupModel.getObject();
                final List<UIRoleForList> roles = rolesHolder.getObject();
                if(checkedRoles.size()==0)return;

                this.getPage().get("confirmPanel").replaceWith(
                        new ConfirmPanel("confirmPanel",
                                "You are about to delete "+checkedRoles.size()
                                        + " role(s) permanently?") {
                            public void onConfirm() {
                                boolean someSuccess = false;
                                for(UIRoleForList uir: roles) {
                                    if (checkedRoles.contains(uir)) {
                                        RoutineResult result = roleService.deleteRole(uir.getId());
                                        if (result.isOk()) {
                                            someSuccess = true;
                                        }
                                    }
                                }
                                if (someSuccess) {
                                    info("Deleted roles; please be aware that some sessions could be invalidated");
                                }
                                this.getPage().setResponsePage(ListRolesPage.class);
                            }
                        });
            }

        });
        form.add(new BookmarkablePageLink<AddRolePage>("add-role", AddRolePage.class));
    }

    @Override
    protected String getTitle() {
        return "Roles";
    }

    private class RoleFilter implements Serializable {
    }
}