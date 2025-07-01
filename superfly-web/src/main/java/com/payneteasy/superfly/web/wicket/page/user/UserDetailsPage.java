package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.util.ArrayList;
import java.util.List;

@Secured("ROLE_ADMIN")
public class UserDetailsPage extends BasePage {
    @SpringBean
    private UserService      userService;
    @SpringBean
    private SubsystemService subsystemService;

    public UserDetailsPage(PageParameters params) {
        super(ListUsersPage.class, params);

        final long userId = params.get("userId").toLong();

        final IModel<UIUserDetails> uiUserDetailsIModel = new LoadableDetachableModel<UIUserDetails>() {
            @Override
            protected UIUserDetails load() {
                return userService.getUser(userId);
            }
        };

        final UIUserWithRolesAndActions user         = userService.getUserRoleActions(userId, null, null, null);
        ListView<String>                subRolesList = getSubRolesList(user, userId);

        add(subRolesList);


//        RIGHT SIDE
        add(new LabelValueRow<UIUserDetails>("username", uiUserDetailsIModel.getObject(), "user.create.username"));
        add(new LabelValueRow<UIUserDetails>("email", uiUserDetailsIModel.getObject(), "user.create.email"));
        add(new LabelValueRow<UIUserDetails>("accountLocked",
                                             uiUserDetailsIModel.getObject(),
                                             "user.create.isAccountLocked"
        ));
        add(new LabelValueRow<UIUserDetails>("name", uiUserDetailsIModel.getObject(), "user.create.name"));
        add(new LabelValueRow<UIUserDetails>("surname", uiUserDetailsIModel.getObject(), "user.create.surname"));
        add(new LabelValueRow<UIUserDetails>("secretQuestion",
                                             uiUserDetailsIModel.getObject(),
                                             "user.create.secret-question"
        ));
        add(new LabelValueRow<UIUserDetails>("secretAnswer",
                                             uiUserDetailsIModel.getObject(),
                                             "user.create.secret-answer"
        ));
        add(new LabelValueRow<UIUserDetails>("publicKey", uiUserDetailsIModel.getObject(), "user.create.publicKey"));
        add(new LabelValueRow<UIUserDetails>("otpName", uiUserDetailsIModel.getObject(), "user.create.otpTypeCode"));
        add(new LabelValueRow<UIUserDetails>("isOtpOptional",
                                             uiUserDetailsIModel.getObject(),
                                             "user.create.isOtpOptional"
        ));


        //ADD SUBSYSTEM
        PageParameters param = new PageParameters();
        param.set("userId", String.valueOf(userId));
        add(new BookmarkablePageLink<AppendSubsystemWithRolePage>("add-sub", AppendSubsystemWithRolePage.class, param));

//        LOCK USER
        Link<Void> switchLockedStatusLink = new Link<Void>("switch-locked-status") {
            @Override
            public void onClick() {
                if (uiUserDetailsIModel.getObject().isAccountLocked()) {
                    String newPassword = userService.unlockUser(uiUserDetailsIModel.getObject().getId(),
                                                                uiUserDetailsIModel.getObject().isAccountSuspended()
                    );
                    String message = "User unlocked: " + uiUserDetailsIModel.getObject().getUsername();
                    if (newPassword != null) {
                        message += "; temporary password is " + newPassword;
                    }
                    info(message);
                } else {
                    RoutineResult result = userService.lockUser(uiUserDetailsIModel.getObject().getId());
                    if (result.isOk()) {
                        info("User locked: " + uiUserDetailsIModel.getObject()
                                                                  .getUsername() + "; please be aware that some sessions could be expired");
                    } else {
                        error("Error while trying to lock a user: " + result.getErrorMessage());
                    }
                }
            }
        };
        switchLockedStatusLink.add(new AttributeAppender("class", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return uiUserDetailsIModel.getObject().isAccountLocked() ? "btn-success" : "btn-danger";
            }
        }, " "
        ));
        switchLockedStatusLink.add(new Label("switch-info", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return uiUserDetailsIModel.getObject().isAccountLocked() ? "Unlock user" : "Lock user";
            }
        }
        ));
        add(switchLockedStatusLink);
        add(new AjaxLink<Void>("reset-otp") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                userService.persistOtpMasterKeyForUsername(uiUserDetailsIModel.getObject().getUsername(), null);
                info("Reset OTP key was successful");
                target.add(getFeedbackPanel());
            }
        });
        final PageParameters actionsParameters = new PageParameters();
        actionsParameters.set("userId", String.valueOf(user.getId()));
        add(new BookmarkablePageLink<EditUserPage>("edit-user", EditUserPage.class, actionsParameters));
        add(new Link<String>("reset-password-link") {
            @Override
            public void onClick() {
                setResponsePage(ResetPasswordUserPage.class, actionsParameters);
            }
        });
    }

    private ListView<String> getSubRolesList(UIUserWithRolesAndActions user, long userId) {
        final List<UIRoleWithActions> roleWithAction = user.getRoles();
        final SortRoleOfSubsystem     sort           = new SortRoleOfSubsystem();
        sort.setRoleWithAction(roleWithAction);

//        LEFT SIDE
        ListView<String> subRolesList = new ListView<String>("sub-list", sort.getSubsystemsName()) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final String rfc = item.getModelObject();
                item.add(new Label("sub-name", rfc));

                final PageParameters actionsParameters = new PageParameters();
                actionsParameters.set("userId", String.valueOf(userId));

                final UISubsystem subsystem = subsystemService.getSubsystemByName(rfc);
                actionsParameters.set("subId", String.valueOf(subsystem.getId()));
                item.add(new BookmarkablePageLink<Page>("add-role", ChangeUserRolesPage.class, actionsParameters));

                List<UIRoleWithActions> roles = sort.getRoles(rfc);
                item.add(new ListView<UIRoleWithActions>("role-list", roles) {

                    @Override
                    protected void populateItem(ListItem<UIRoleWithActions> listItem) {
                        final UIRoleWithActions role   = listItem.getModelObject();
                        PageParameters          params = new PageParameters();
                        params.set("userId", String.valueOf(userId));
                        params.set("subId", String.valueOf(subsystem.getId()));
                        params.set("roleId", String.valueOf(role.getId()));
                        listItem.add(new BookmarkablePageLink<ChangeUserGrantActionsPage>("grant-user-action",
                                                                                          ChangeUserGrantActionsPage.class,
                                                                                          params
                        ));
                        listItem.add(new Label("role-name", role.getName()));
                        listItem.add(new Link<Void>("delete-role") {

                            @Override
                            public void onClick() {
                                List<Long> rolesId = new ArrayList<Long>();
                                rolesId.add(role.getId());
                                userService.changeUserRoles(userId, null, rolesId, null);
                                PageParameters parameters = new PageParameters();
                                parameters.set("userId", String.valueOf(userId));
                                setResponsePage(UserDetailsPage.class, parameters);
                            }
                        });
                    }
                });
            }
        };
        return subRolesList;
    }

    @Override
    protected String getTitle() {
        return "User details";
    }

}
