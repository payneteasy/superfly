package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.common.utils.UserNames;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The admin pages refuse a user name that does not fit the database column instead of letting the database
 * fail or cut it.
 */
public class UserNameLengthValidationTest extends AbstractPageTest {
    private static final String FIELD = "username:row:field-id";

    private UserService userService;
    private RoleService roleService;
    private SubsystemService subsystemService;
    private SettingsService settingsService;
    private PublicKeyCrypto crypto;
    private IPolicyValidation<?> policyValidation;

    @Before
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
        userService = createNiceMock(UserService.class);
        roleService = createNiceMock(RoleService.class);
        subsystemService = createNiceMock(SubsystemService.class);
        settingsService = createNiceMock(SettingsService.class);
        crypto = createNiceMock(PublicKeyCrypto.class);
        policyValidation = createNiceMock(IPolicyValidation.class);

        UIUserDetails user = new UIUserDetails();
        user.setId(1L);
        user.setUsername("bond");
        expect(userService.getUser(1L)).andStubReturn(user);
        expect(subsystemService.getSubsystemsForFilter()).andStubReturn(Collections.emptyList());
        expect(settingsService.getSuperflyVersion()).andStubReturn("test");
        replay(userService, roleService, subsystemService, settingsService, crypto, policyValidation);
        // the labels live in the production application's bundle, which the test application does not have
        tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == UserService.class) {
            return userService;
        }
        if (type == RoleService.class) {
            return roleService;
        }
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        if (type == PublicKeyCrypto.class) {
            return crypto;
        }
        if (type == IPolicyValidation.class) {
            return policyValidation;
        }
        return super.getBean(type);
    }

    private boolean usernameRejected(String username) {
        FormTester form = tester.newFormTester("form");
        form.setValue(FIELD, username);
        form.submit();
        Component field = tester.getComponentFromLastRenderedPage("form:" + FIELD);
        return !tester.getFeedbackMessages(new ComponentFeedbackMessageFilter(field)).isEmpty();
    }

    private static PageParameters userIdParams() {
        return new PageParameters().add("userId", 1L);
    }

    private static String tooLong() {
        return "a".repeat(UserNames.MAX_LENGTH + 1);
    }

    private static String longest() {
        return "a".repeat(UserNames.MAX_LENGTH);
    }

    @Test
    public void createUserPageRefusesTooLongName() {
        tester.startPage(CreateUserPage.class);
        assertTrue(usernameRejected(tooLong()));
        tester.startPage(CreateUserPage.class);
        assertFalse(usernameRejected(longest()));
    }

    @Test
    public void editUserPageRefusesTooLongName() {
        tester.startPage(EditUserPage.class, userIdParams());
        assertTrue(usernameRejected(tooLong()));
        tester.startPage(EditUserPage.class, userIdParams());
        assertFalse(usernameRejected(longest()));
    }

    @Test
    public void cloneUserPageRefusesTooLongName() {
        tester.startPage(CloneUserPage.class, userIdParams());
        assertTrue(usernameRejected(tooLong()));
        tester.startPage(CloneUserPage.class, userIdParams());
        assertFalse(usernameRejected(longest()));
    }
}
