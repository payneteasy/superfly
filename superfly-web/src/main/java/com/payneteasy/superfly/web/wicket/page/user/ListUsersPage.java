package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.RoleChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.label.DateLabels;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import com.payneteasy.superfly.web.wicket.utils.WicketComponentHelper;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.springframework.security.access.annotation.Secured;

import java.io.*;
import java.util.Iterator;

/**
 * Displays a list of users.
 *
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ListUsersPage extends BasePage {
    @SpringBean
    private UserService userService;
    @SpringBean
    private RoleService roleService;
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private HOTPProvider hotpProvider;

    public ListUsersPage() {
        super(ListUsersPage.class);

        final ModalWindow resetHotpWindow = new ModalWindow("reset-hotp-window");
        add(resetHotpWindow);

        // filters
        final UserFilters userFilters = new UserFilters();
        Form<UserFilters> filtersForm = new Form<UserFilters>("filters-form");
        add(filtersForm);
        filtersForm.add(new TextField<String>("username-filter", new PropertyModel<String>(userFilters, "usernamePrefix")));

        DropDownChoice<UIRoleForFilter> roleDropdown = new DropDownChoice<UIRoleForFilter>(
                "role-filter"
                , new PropertyModel<UIRoleForFilter>(userFilters, "role")
                , roleService.getRolesForFilter(), new RoleChoiceRenderer()
        );
        roleDropdown.setNullValid(true);
        filtersForm.add(roleDropdown);

        DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
                "subsystem-filter"
                , new PropertyModel<UISubsystemForFilter>(userFilters, "subsystem")
                , subsystemService.getSubsystemsForFilter(), new SubsystemChoiceRenderer()
        );
        subsystemDropdown.setNullValid(true);
        filtersForm.add(subsystemDropdown);

        // data provider + sortability
        String[] fieldNames = {"userId", "username", "password", "locked", "loginsFailed", "lastLoginDate"};

        SortableDataProvider<UIUserForList, String> usersDataProvider = new IndexedSortableDataProvider<UIUserForList>(fieldNames) {
            public Iterator<? extends UIUserForList> iterator(long first, long count) {
                UIRoleForFilter role = userFilters.getRole();
                UISubsystemForFilter subsystem = userFilters.getSubsystem();
                return userService.getUsers(userFilters.getUsernamePrefix(),
                        role == null ? null : role.getId(), null,
                        subsystem == null ? null : subsystem.getId(),
                        first, count, getSortFieldIndex(), isAscending()).iterator();
            }

            public long size() {
                UIRoleForFilter role = userFilters.getRole();
                UISubsystemForFilter subsystem = userFilters.getSubsystem();
                return userService.getUsersCount(userFilters.getUsernamePrefix(),
                        role == null ? null : role.getId(), null,
                        subsystem == null ? null : subsystem.getId());
            }
        };
        usersDataProvider.setSort(new SortParam<String>("loginsFailed", true)); //by default

        // data itself
        DataView<UIUserForList> usersDataView = new PagingDataView<UIUserForList>("usersList", usersDataProvider) {
            @Override
            protected void populateItem(Item<UIUserForList> item) {
                item.setOutputMarkupId(true);
                final UIUserForList user = item.getModelObject();
                final PageParameters actionsParameters = new PageParameters();
                actionsParameters.set("userId", String.valueOf(user.getId()));
                item.add(new Label("user-name", user.getUsername()));

                checkAccountForLocked(item, user);

                item.add(new Label("logins-failed", String.valueOf(user.getLoginsFailed())));
                item.add(DateLabels.forDateTime("last-login-date", user.getLastLoginDate()));
                item.add(new Label("next-otp-counter", String.valueOf(user.getNextOtpCounter())));
                item.add(new Label("email", user.getEmail()));

//                ACTIONS
                item.add(new BookmarkablePageLink<EditUserPage>("edit-user", EditUserPage.class, actionsParameters));
                item.add(new BookmarkablePageLink<CloneUserPage>("clone-user", CloneUserPage.class, actionsParameters));
                Link<String> resetLink = new Link<String>("reset-password-link") {
                    @Override
                    public void onClick() {
                        setResponsePage(ResetPasswordUserPage.class, actionsParameters);
                    }
                };
                item.add(resetLink);

                downloadHtopTable(item, user);

                resetHoptTable(item, user, resetHotpWindow);

                WicketComponentHelper.clickTableRow(item, UserDetailsPage.class, actionsParameters, this);

            }
        };
        add(usersDataView);

        // ordering, paging...
        add(new OrderByLink("order-by-username", "username", usersDataProvider));
        add(new OrderByLink("order-by-logins-failed", "loginsFailed", usersDataProvider));
        add(new OrderByLink("order-by-last-login-date", "lastLoginDate", usersDataProvider));

        //add(new PagingNavigator("paging-navigator", usersDataView));
        add(new SuperflyPagingNavigator("paging-navigator", usersDataView));

        add(new BookmarkablePageLink<CreateUserPage>("add-user", CreateUserPage.class));
    }



    @Override
    protected String getTitle() {
        return "Users";
    }

    //PRIVATE METHODS
    private void checkAccountForLocked(Item<UIUserForList> item, UIUserForList user) {
        if(user.isAccountLocked()){
            item.add(new AttributeModifier("class", new Model<String>("error")));
        }
    }

    private void resetHoptTable(Item<UIUserForList> item, final UIUserForList user, final ModalWindow resetHotpWindow) {
        AjaxLink<Void> resetTableLink = new AjaxLink<Void>("reset-table-link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                resetHotpWindow.setContent(new ResetOtpTablePanel(resetHotpWindow.getContentId(),
                        user.getId(), resetHotpWindow, getFeedbackPanel()));
                resetHotpWindow.show(target);
            }
        };
        resetTableLink.setVisible(hotpProvider.outputsSequenceForDownload());
        item.add(resetTableLink);
    }

    private void downloadHtopTable(Item<UIUserForList> item, final UIUserForList user) {
        Link<Void> downloadHotpTableLink = new Link<Void>("download-hotp-table") {
            @Override
            public void onClick() {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    hotpProvider.outputSequenceForDownload(user.getUsername(), os);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                final byte[] bytes = os.toByteArray();
                IResourceStream resourceStream = new AbstractResourceStream() {
                    @Override
                    public Time lastModifiedTime() {
                        return Time.now();
                    }

                    @Override
                    public Bytes length() {
                        return Bytes.bytes(bytes.length);
                    }

                    @Override
                    public InputStream getInputStream() throws ResourceStreamNotFoundException {
                        return new ByteArrayInputStream(bytes);
                    }

                    @Override
                    public String getContentType() {
                        return "application/vnd.ms-excel";
                    }

                    @Override
                    public void close() throws IOException {
                    }
                };
                getRequestCycle().replaceAllRequestHandlers(new ResourceStreamRequestHandler(resourceStream,
                        hotpProvider.getSequenceForDownloadFileName(user.getUsername())));
            }
        };
        downloadHotpTableLink.setVisible(hotpProvider.outputsSequenceForDownload());
        item.add(downloadHotpTableLink);
    }

    @SuppressWarnings("unused")
    private class UserFilters implements Serializable {
        private String usernamePrefix;
        private UIRoleForFilter role;
        private UISubsystemForFilter subsystem;

        public String getUsernamePrefix() {
            return usernamePrefix;
        }

        public void setUsernamePrefix(String usernamePrefix) {
            this.usernamePrefix = usernamePrefix;
        }

        public UIRoleForFilter getRole() {
            return role;
        }

        public void setRole(UIRoleForFilter role) {
            this.role = role;
        }

        public UISubsystemForFilter getSubsystem() {
            return subsystem;
        }

        public void setSubsystem(UISubsystemForFilter subsystem) {
            this.subsystem = subsystem;
        }
    }

}
