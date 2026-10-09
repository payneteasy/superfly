package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.common.utils.UserNames;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * A name that cannot exist (empty or longer than the database column) gets the unknown-user answer of every
 * method and never reaches the underlying service.
 */
@RunWith(Parameterized.class)
public class SSOServiceImplUserNameLimitTest {

    @Parameterized.Parameters(name = "length {index}")
    public static Collection<Object[]> names() {
        return Arrays.asList(new Object[][]{{"a".repeat(UserNames.MAX_LENGTH + 1)}, {""}});
    }

    @Parameterized.Parameter
    public String name;

    private SSOServiceImpl        ssoService;
    private InternalSSOService    internal;
    private HOTPService           hotpService;
    private ResetPasswordStrategy resetPasswordStrategy;

    @Before
    public void setUp() {
        internal = createStrictMock(InternalSSOService.class);
        hotpService = createStrictMock(HOTPService.class);
        resetPasswordStrategy = createStrictMock(ResetPasswordStrategy.class);
        ssoService = new SSOServiceImpl(internal, hotpService, resetPasswordStrategy, null, null);
        ssoService.setSubsystemIdentifierObtainer(hint -> "caller");
    }

    private void replayAll() {
        replay(internal, hotpService, resetPasswordStrategy);
    }

    private void verifyAll() {
        verify(internal, hotpService, resetPasswordStrategy);
    }

    @Test
    public void checkOtp() throws Exception {
        replayAll();
        assertEquals(CheckOtpResult.Status.INVALID,
                ssoService.checkOtp(new CheckOtpRequest(name, "abcdef", OTPType.GOOGLE_AUTH, false)).getStatus());
        verifyAll();
    }

    @Test
    public void hasOtpMasterKey() {
        replayAll();
        assertFalse(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest(name)));
        verifyAll();
    }

    @Test
    public void updateUserOtpType() {
        replayAll();
        ssoService.updateUserOtpType(new UpdateUserOtpTypeRequest(name, "GOOGLE_AUTH"));
        verifyAll();
    }

    @Test
    public void changeTempPasswordStillValidatesPolicy() throws Exception {
        internal.validatePasswordPolicy(null, "pw");
        replayAll();
        ssoService.changeTempPassword(new ChangeTempPasswordRequest(name, "pw"));
        verifyAll();
    }

    @Test
    public void getUserDescription() {
        replayAll();
        assertNull(ssoService.getUserDescription(GetUserDescriptionRequest.builder().username(name).build()));
        verifyAll();
    }

    @Test
    public void resetGoogleAuthMasterKeyGivesUnsavedKey() throws Exception {
        replayAll();
        assertNotNull(ssoService.resetGoogleAuthMasterKey(new ResetGoogleAuthMasterKeyRequest(name)));
        verifyAll();
    }

    @Test
    public void confirmOtpMasterKey() throws Exception {
        replayAll();
        assertEquals(CheckOtpResult.Status.INVALID,
                ssoService.confirmOtpMasterKey(new ConfirmOtpMasterKeyRequest(name, "123456")).getStatus());
        verifyAll();
    }

    @Test
    public void updateUserIsOtpOptionalValue() {
        replayAll();
        ssoService.updateUserIsOtpOptionalValue(new UpdateUserIsOtpOptionalValueRequest(name, true));
        verifyAll();
    }

    @Test(expected = UserNotFoundException.class)
    public void updateUserDescription() throws Exception {
        replayAll();
        UserDescription description = new UserDescription();
        description.setUsername(name);
        ssoService.updateUserDescription(new UpdateUserDescriptionRequest(description));
    }

    @Test(expected = UserNotFoundException.class)
    public void resetPassword() throws Exception {
        replayAll();
        ssoService.resetPassword(PasswordResetRequest.builder().username(name).password("pw").build());
    }

    @Test
    public void getUserStatuses() {
        replayAll();
        assertTrue(ssoService.getUserStatuses(new GetUserStatusesRequest(Collections.singletonList(name))).isEmpty());
        verifyAll();
    }

    @Test
    public void completeUser() {
        replayAll();
        ssoService.completeUser(CompleteUserRequest.builder().username(name).build());
        verifyAll();
    }

    @Test
    public void changeUserRole() {
        replayAll();
        try {
            ssoService.changeUserRole(ChangeUserRoleRequest.builder().username(name).newRole("role").build());
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("Cannot find user by name", expected.getMessage());
        }
        verifyAll();
    }
}
