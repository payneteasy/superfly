package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CreateUserPageTest extends AbstractPageTest {

    private static final String OTP_TYPE_PATH = "form:otpType:container:field-id";
    private static final String OTP_TYPE_ROW_PATH = "form:otpType";
    private static final String OTP_OPTIONAL_PATH = "form:isOtpOptional:field-id";

    private UserService userService;
    private RoleService roleService;
    private SubsystemService subsystemService;
    private SettingsService settingsService;
    private PublicKeyCrypto crypto;

    @Before
    public void setUpBeans() {
        userService = EasyMock.createNiceMock(UserService.class);
        roleService = EasyMock.createNiceMock(RoleService.class);
        subsystemService = EasyMock.createMock(SubsystemService.class);
        settingsService = EasyMock.createNiceMock(SettingsService.class);
        crypto = EasyMock.createNiceMock(PublicKeyCrypto.class);

        expect(subsystemService.getSubsystemsForFilter()).andReturn(Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "admin", AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
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
        return super.getBean(type);
    }

    private void startPage() {
        EasyMock.replay(userService, roleService, subsystemService, settingsService, crypto);
        tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
        tester.startPage(CreateUserPage.class);
        tester.assertRenderedPage(CreateUserPage.class);
    }

    @Test
    public void otpIsOptionalByDefaultOnTheCreateForm() {
        startPage();

        CheckBox checkBox = (CheckBox) tester.getComponentFromLastRenderedPage(OTP_OPTIONAL_PATH);
        assertEquals(Boolean.TRUE, checkBox.getDefaultModelObject());
    }

    @Test
    public void clearingOptionalAutoSetsGoogleAuth() {
        startPage();

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("isOtpOptional:field-id", false);
        tester.executeAjaxEvent(OTP_OPTIONAL_PATH, "change");

        DropDownChoice<?> otpType = (DropDownChoice<?>) tester.getComponentFromLastRenderedPage(OTP_TYPE_PATH);
        assertEquals(OTPType.GOOGLE_AUTH.code(), otpType.getDefaultModelObject());
        tester.assertComponentOnAjaxResponse(OTP_TYPE_ROW_PATH);
        assertFalse("expected an info message about the automatically set OTP type",
                tester.getMessages(FeedbackMessage.INFO).isEmpty());
    }

    @Test
    public void choosingNoneWhileOtpIsMandatoryRevertsToGoogleAuth() {
        startPage();

        FormTester optionalTester = tester.newFormTester("form");
        optionalTester.setValue("isOtpOptional:field-id", false);
        tester.executeAjaxEvent(OTP_OPTIONAL_PATH, "change");

        FormTester typeTester = tester.newFormTester("form");
        // otpTypes() lists OTPType.values() in declaration order, so the ordinal is the choice index
        typeTester.select("otpType:container:field-id", OTPType.NONE.ordinal());
        tester.executeAjaxEvent(OTP_TYPE_PATH, "change");

        DropDownChoice<?> otpType = (DropDownChoice<?>) tester.getComponentFromLastRenderedPage(OTP_TYPE_PATH);
        assertEquals(OTPType.GOOGLE_AUTH.code(), otpType.getDefaultModelObject());
    }
}
