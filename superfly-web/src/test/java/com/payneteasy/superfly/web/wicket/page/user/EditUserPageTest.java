package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

public class EditUserPageTest extends AbstractPageTest {

    private static final String OTP_TYPE_PATH = "form:otpType:container:field-id";
    private static final String OTP_OPTIONAL_PATH = "form:isOtpOptional:field-id";

    private UserService userService;
    private SettingsService settingsService;
    private PublicKeyCrypto crypto;

    @Before
    public void setUpBeans() {
        userService = EasyMock.createMock(UserService.class);
        settingsService = EasyMock.createNiceMock(SettingsService.class);
        crypto = EasyMock.createNiceMock(PublicKeyCrypto.class);

        // BasePage renders the logged-in user name from the security context
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
        if (type == SettingsService.class) {
            return settingsService;
        }
        if (type == PublicKeyCrypto.class) {
            return crypto;
        }
        return super.getBean(type);
    }

    private void startPageForUser(String otpTypeCode, boolean isOtpOptional) {
        UIUserDetails user = new UIUserDetails();
        user.setId(1L);
        user.setUsername("john");
        user.setOtpType(otpTypeCode);
        user.setOtpOptional(isOtpOptional);
        expect(userService.getUser(1L)).andReturn(user);
        EasyMock.replay(userService, settingsService, crypto);

        // MockApplication does not load the app-level SuperflyApplication.properties bundle,
        // and this test only cares about the OTP wiring, not label texts
        tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
        tester.startPage(EditUserPage.class, new PageParameters().add("userId", 1L));
        tester.assertRenderedPage(EditUserPage.class);
    }

    private void setOtpOptionalAndFireAjax(boolean optional) {
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("isOtpOptional:field-id", optional);
        tester.executeAjaxEvent(OTP_OPTIONAL_PATH, "change");
    }

    private Object renderedOtpType() {
        return tester.getComponentFromLastRenderedPage(OTP_TYPE_PATH).getDefaultModelObject();
    }

    @Test
    public void makingOtpMandatoryAutoSetsGoogleAuthWhenTypeIsNone() {
        startPageForUser(OTPType.NONE.code(), true);

        setOtpOptionalAndFireAjax(false);

        assertEquals(OTPType.GOOGLE_AUTH.code(), renderedOtpType());
    }

    @Test
    public void makingOtpMandatoryKeepsAlreadyChosenType() {
        startPageForUser(OTPType.GOOGLE_AUTH.code(), true);

        setOtpOptionalAndFireAjax(false);

        assertEquals(OTPType.GOOGLE_AUTH.code(), renderedOtpType());
    }

    @Test
    public void keepingOtpOptionalDoesNotChangeType() {
        startPageForUser(OTPType.NONE.code(), false);

        setOtpOptionalAndFireAjax(true);

        assertEquals(OTPType.NONE.code(), renderedOtpType());
    }
}
