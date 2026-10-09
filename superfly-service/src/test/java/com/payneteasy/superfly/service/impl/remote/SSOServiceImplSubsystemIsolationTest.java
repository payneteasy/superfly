package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.UserStatus;
import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * A subsystem may only act on users that have a role in it; users that also have a role in the local subsystem
 * are signed in and read, but never changed. Every denial must look exactly like "no such user" and must not
 * reach the underlying service.
 */
public class SSOServiceImplSubsystemIsolationTest {
    private static final String CALLER = "caller";
    private static final String LOCAL  = "superfly";
    private static final String USER   = "victim";

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
        ssoService.setSubsystemIdentifierObtainer(hint -> CALLER);
    }

    // sign-in and reads

    private void expectForeign() {
        expect(internal.isUserAccessibleFrom(USER, CALLER)).andReturn(false);
    }

    private void expectSuperflyAdminWithRole() {
        expect(internal.isUserAccessibleFrom(USER, CALLER)).andReturn(true);
    }

    private void expectOwn() {
        expect(internal.isUserAccessibleFrom(USER, CALLER)).andReturn(true);
    }

    // changes of password, OTP, profile and roles

    private void expectForeignForChange() {
        expect(internal.isUserManageableFrom(USER, CALLER)).andReturn(false);
    }

    private void expectSuperflyAdminWithRoleForChange() {
        expect(internal.isUserManageableFrom(USER, CALLER)).andReturn(false);
    }

    private void expectOwnForChange() {
        expect(internal.isUserManageableFrom(USER, CALLER)).andReturn(true);
    }

    private void replayAll() {
        replay(internal, hotpService, resetPasswordStrategy);
    }

    private void verifyAll() {
        verify(internal, hotpService, resetPasswordStrategy);
    }

    private static UserForDescription userForDescription() {
        UserForDescription user = new UserForDescription();
        user.setUserId(7L);
        user.setUsername(USER);
        return user;
    }

    // checkOtp

    @Test
    public void checkOtpForeignUserBehavesLikeUnknownUser() throws Exception {
        expectForeign();
        replayAll();
        assertEquals(CheckOtpResult.Status.INVALID, ssoService.checkOtp(new CheckOtpRequest(USER, "abcdef", OTPType.GOOGLE_AUTH, false)).getStatus());
        verifyAll();
    }

    @Test
    public void checkOtpForeignUserWithWellFormedCodeFailsLikeUnknownUser() throws Exception {
        expectForeign();
        replayAll();
        try {
            ssoService.checkOtp(new CheckOtpRequest(USER, "123456", OTPType.GOOGLE_AUTH, false));
            fail();
        } catch (SsoDecryptException expected) {
            assertTrue(expected.getMessage().contains(USER));
        }
        verifyAll();
    }

    @Test
    public void checkOtpSuperflyAdminWithRoleIsDelegated() throws Exception {
        expectSuperflyAdminWithRole();
        expect(internal.checkOtp(OTPType.GOOGLE_AUTH, false, USER, "abcdef")).andReturn(CheckOtpResult.Status.SUCCESS);
        replayAll();
        assertEquals(CheckOtpResult.Status.SUCCESS, ssoService.checkOtp(new CheckOtpRequest(USER, "abcdef", OTPType.GOOGLE_AUTH, false)).getStatus());
        verifyAll();
    }

    @Test
    public void checkOtpOwnUserIsDelegated() throws Exception {
        expectOwn();
        expect(internal.checkOtp(OTPType.GOOGLE_AUTH, false, USER, "123456")).andReturn(CheckOtpResult.Status.SUCCESS);
        replayAll();
        assertEquals(CheckOtpResult.Status.SUCCESS, ssoService.checkOtp(new CheckOtpRequest(USER, "123456", OTPType.GOOGLE_AUTH, false)).getStatus());
        verifyAll();
    }

    // hasOtpMasterKey

    @Test
    public void hasOtpMasterKeyForeignUser() {
        expectForeign();
        replayAll();
        assertFalse(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest(USER)));
        verifyAll();
    }

    @Test
    public void hasOtpMasterKeySuperflyAdminWithRole() {
        expectSuperflyAdminWithRole();
        expect(internal.hasOtpMasterKey(USER)).andReturn(true);
        replayAll();
        assertTrue(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest(USER)));
        verifyAll();
    }

    @Test
    public void hasOtpMasterKeyOwnUser() {
        expectOwn();
        expect(internal.hasOtpMasterKey(USER)).andReturn(true);
        replayAll();
        assertTrue(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest(USER)));
        verifyAll();
    }

    // updateUserOtpType

    @Test
    public void updateUserOtpTypeForeignUser() {
        expectForeignForChange();
        replayAll();
        ssoService.updateUserOtpType(new UpdateUserOtpTypeRequest(USER, "GOOGLE_AUTH"));
        verifyAll();
    }

    @Test
    public void updateUserOtpTypeSuperflyAdminWithRole() {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        ssoService.updateUserOtpType(new UpdateUserOtpTypeRequest(USER, "GOOGLE_AUTH"));
        verifyAll();
    }

    @Test
    public void updateUserOtpTypeOwnUser() {
        expectOwnForChange();
        internal.updateUserOtpType(USER, "GOOGLE_AUTH");
        replayAll();
        ssoService.updateUserOtpType(new UpdateUserOtpTypeRequest(USER, "GOOGLE_AUTH"));
        verifyAll();
    }

    // changeTempPassword

    @Test
    public void changeTempPasswordForeignUserStillValidatesPolicy() throws Exception {
        expectForeignForChange();
        internal.validatePasswordPolicy(null, "pw");
        replayAll();
        ssoService.changeTempPassword(new ChangeTempPasswordRequest(USER, "pw"));
        verifyAll();
    }

    @Test
    public void changeTempPasswordForeignUserWeakPasswordFailsLikeUnknownUser() throws Exception {
        expectForeignForChange();
        internal.validatePasswordPolicy(null, "pw");
        expectLastCall().andThrow(new PolicyValidationException("weak"));
        replayAll();
        try {
            ssoService.changeTempPassword(new ChangeTempPasswordRequest(USER, "pw"));
            fail();
        } catch (PolicyValidationException expected) {
            // same as for an unknown user
        }
        verifyAll();
    }

    @Test
    public void changeTempPasswordSuperflyAdminWithRole() throws Exception {
        expectSuperflyAdminWithRoleForChange();
        internal.validatePasswordPolicy(null, "pw");
        replayAll();
        ssoService.changeTempPassword(new ChangeTempPasswordRequest(USER, "pw"));
        verifyAll();
    }

    @Test
    public void changeTempPasswordOwnUser() throws Exception {
        expectOwnForChange();
        internal.changeTempPassword(USER, "pw");
        replayAll();
        ssoService.changeTempPassword(new ChangeTempPasswordRequest(USER, "pw"));
        verifyAll();
    }

    // getUserDescription

    @Test
    public void getUserDescriptionForeignUser() {
        expectForeign();
        replayAll();
        assertNull(ssoService.getUserDescription(GetUserDescriptionRequest.builder().username(USER).build()));
        verifyAll();
    }

    @Test
    public void getUserDescriptionSuperflyAdminWithRole() {
        expectSuperflyAdminWithRole();
        expect(internal.getUserDescription(USER)).andReturn(userForDescription());
        replayAll();
        UserDescription description =
                ssoService.getUserDescription(GetUserDescriptionRequest.builder().username(USER).build());
        assertEquals(USER, description.getUsername());
        verifyAll();
    }

    @Test
    public void getUserDescriptionOwnUser() {
        expectOwn();
        expect(internal.getUserDescription(USER)).andReturn(userForDescription());
        replayAll();
        UserDescription description =
                ssoService.getUserDescription(GetUserDescriptionRequest.builder().username(USER).build());
        assertEquals(USER, description.getUsername());
        verifyAll();
    }

    // resetGoogleAuthMasterKey

    @Test
    public void resetGoogleAuthMasterKeyForeignUserReturnsKeyAndPersistsNothing() throws Exception {
        expectForeignForChange();
        replayAll();
        String key = ssoService.resetGoogleAuthMasterKey(new ResetGoogleAuthMasterKeyRequest(USER));
        assertNotNull(key);
        assertFalse(key.isEmpty());
        verifyAll();
    }

    @Test
    public void resetGoogleAuthMasterKeySuperflyAdminWithRole() throws Exception {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        assertNotNull(ssoService.resetGoogleAuthMasterKey(new ResetGoogleAuthMasterKeyRequest(USER)));
        verifyAll();
    }

    @Test
    public void resetGoogleAuthMasterKeyOwnUser() throws Exception {
        expectOwnForChange();
        expect(hotpService.resetGoogleAuthMasterKey(CALLER, USER)).andReturn("key");
        replayAll();
        assertEquals("key", ssoService.resetGoogleAuthMasterKey(new ResetGoogleAuthMasterKeyRequest(USER)));
        verifyAll();
    }

    // confirmOtpMasterKey

    @Test
    public void confirmOtpMasterKeyForeignUserIsInvalid() throws Exception {
        expectForeignForChange();
        replayAll();
        assertEquals(CheckOtpResult.Status.INVALID,
                ssoService.confirmOtpMasterKey(new ConfirmOtpMasterKeyRequest(USER, "123456")).getStatus());
        verifyAll();
    }

    @Test
    public void confirmOtpMasterKeySuperflyAdminWithRoleIsInvalid() throws Exception {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        assertEquals(CheckOtpResult.Status.INVALID,
                ssoService.confirmOtpMasterKey(new ConfirmOtpMasterKeyRequest(USER, "123456")).getStatus());
        verifyAll();
    }

    @Test
    public void confirmOtpMasterKeyOwnUserIsDelegated() throws Exception {
        expectOwnForChange();
        expect(internal.confirmOtpMasterKey(USER, "123456")).andReturn(CheckOtpResult.Status.LOCKED);
        replayAll();
        assertEquals(CheckOtpResult.Status.LOCKED,
                ssoService.confirmOtpMasterKey(new ConfirmOtpMasterKeyRequest(USER, "123456")).getStatus());
        verifyAll();
    }

    // updateUserIsOtpOptionalValue

    @Test
    public void updateUserIsOtpOptionalValueForeignUser() {
        expectForeignForChange();
        replayAll();
        ssoService.updateUserIsOtpOptionalValue(new UpdateUserIsOtpOptionalValueRequest(USER, true));
        verifyAll();
    }

    @Test
    public void updateUserIsOtpOptionalValueSuperflyAdminWithRole() {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        ssoService.updateUserIsOtpOptionalValue(new UpdateUserIsOtpOptionalValueRequest(USER, true));
        verifyAll();
    }

    @Test
    public void updateUserIsOtpOptionalValueOwnUser() {
        expectOwnForChange();
        internal.updateUserIsOtpOptionalValue(USER, true);
        replayAll();
        ssoService.updateUserIsOtpOptionalValue(new UpdateUserIsOtpOptionalValueRequest(USER, true));
        verifyAll();
    }

    // updateUserDescription

    private static UpdateUserDescriptionRequest updateDescriptionRequest() {
        UserDescription description = new UserDescription();
        description.setUsername(USER);
        return new UpdateUserDescriptionRequest(description);
    }

    @Test
    public void updateUserDescriptionForeignUserNotFound() throws Exception {
        expectForeignForChange();
        replayAll();
        try {
            ssoService.updateUserDescription(updateDescriptionRequest());
            fail();
        } catch (UserNotFoundException expected) {
            // same as for an unknown user
        }
        verifyAll();
    }

    @Test
    public void updateUserDescriptionSuperflyAdminWithRoleNotFound() throws Exception {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        try {
            ssoService.updateUserDescription(updateDescriptionRequest());
            fail();
        } catch (UserNotFoundException expected) {
            // same as for an unknown user
        }
        verifyAll();
    }

    @Test
    public void updateUserDescriptionOwnUser() throws Exception {
        expectOwnForChange();
        UserForDescription user = userForDescription();
        expect(internal.getUserDescription(USER)).andReturn(user);
        internal.updateUserForDescription(user);
        replayAll();
        ssoService.updateUserDescription(updateDescriptionRequest());
        verifyAll();
    }

    // resetPassword

    private static PasswordResetRequest resetRequest(String password) {
        return PasswordResetRequest.builder().username(USER).password(password).build();
    }

    @Test
    public void resetPasswordForeignUserNotFound() throws Exception {
        expectForeignForChange();
        replayAll();
        try {
            ssoService.resetPassword(resetRequest("pw"));
            fail();
        } catch (UserNotFoundException expected) {
            // same as for an unknown user
        }
        verifyAll();
    }

    @Test
    public void resetPasswordSuperflyAdminWithRoleNotFound() throws Exception {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        try {
            ssoService.resetPassword(resetRequest("pw"));
            fail();
        } catch (UserNotFoundException expected) {
            // same as for an unknown user
        }
        verifyAll();
    }

    @Test
    public void resetPasswordOwnUserValidatesPolicyBeforeReset() throws Exception {
        expectOwnForChange();
        expect(internal.getUserDescription(USER)).andReturn(userForDescription());
        internal.validatePasswordPolicy(USER, "pw");
        resetPasswordStrategy.resetPassword(7L, USER, "pw");
        replayAll();
        ssoService.resetPassword(resetRequest("pw"));
        verifyAll();
    }

    @Test
    public void resetPasswordRejectedByPolicyIsNotApplied() throws Exception {
        expectOwnForChange();
        expect(internal.getUserDescription(USER)).andReturn(userForDescription());
        internal.validatePasswordPolicy(USER, "pw");
        expectLastCall().andThrow(new PolicyValidationException("weak"));
        replayAll();
        try {
            ssoService.resetPassword(resetRequest("pw"));
            fail();
        } catch (PolicyValidationException expected) {
            // password was not reset: the strict mock has no resetPassword expectation
        }
        verifyAll();
    }

    // completeUser

    @Test
    public void completeUserForeignUser() {
        expectForeignForChange();
        replayAll();
        ssoService.completeUser(CompleteUserRequest.builder().username(USER).build());
        verifyAll();
    }

    @Test
    public void completeUserSuperflyAdminWithRole() {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        ssoService.completeUser(CompleteUserRequest.builder().username(USER).build());
        verifyAll();
    }

    @Test
    public void completeUserOwnUser() {
        expectOwnForChange();
        internal.completeUser(USER);
        replayAll();
        ssoService.completeUser(CompleteUserRequest.builder().username(USER).build());
        verifyAll();
    }

    // changeUserRole

    private static ChangeUserRoleRequest changeRoleRequest() {
        return ChangeUserRoleRequest.builder().username(USER).newRole("ROLE").build();
    }

    @Test
    public void changeUserRoleForeignUserFailsLikeUnknownUser() {
        expectForeignForChange();
        replayAll();
        try {
            ssoService.changeUserRole(changeRoleRequest());
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("Cannot find user by name", expected.getMessage());
        }
        verifyAll();
    }

    @Test
    public void changeUserRoleSuperflyAdminWithRoleFailsLikeUnknownUser() {
        expectSuperflyAdminWithRoleForChange();
        replayAll();
        try {
            ssoService.changeUserRole(changeRoleRequest());
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("Cannot find user by name", expected.getMessage());
        }
        verifyAll();
    }

    @Test
    public void changeUserRoleOwnUserIsDelegated() {
        expectOwnForChange();
        internal.changeUserRole(USER, "ROLE", CALLER);
        replayAll();
        ssoService.changeUserRole(changeRoleRequest());
        verifyAll();
    }

    // getUserStatuses

    @Test
    public void getUserStatusesWithoutNamesReturnsNothing() {
        replayAll();
        assertTrue(ssoService.getUserStatuses(new GetUserStatusesRequest(null)).isEmpty());
        verifyAll();
    }

    @Test
    public void getUserStatusesOnlyForeignUsersAndAdminsWithoutRoleReturnsNothing() {
        expect(internal.isUserAccessibleFrom("foreign", CALLER)).andReturn(false);
        expect(internal.isUserAccessibleFrom("admin", CALLER)).andReturn(false);
        replayAll();
        assertTrue(ssoService.getUserStatuses(new GetUserStatusesRequest(Arrays.asList("foreign", "admin"))).isEmpty());
        verifyAll();
    }

    @Test
    public void getUserStatusesQueriesOnlyOwnUsers() {
        expect(internal.isUserAccessibleFrom("own", CALLER)).andReturn(true);
        expect(internal.isUserAccessibleFrom("foreign", CALLER)).andReturn(false);
        UserWithStatus status = new UserWithStatus();
        status.setUserName("own");
        expect(internal.getUserStatuses("own")).andReturn(Collections.singletonList(status));
        replayAll();
        List<UserStatus> result =
                ssoService.getUserStatuses(new GetUserStatusesRequest(Arrays.asList("own", "foreign")));
        assertEquals(1, result.size());
        assertEquals("own", result.get(0).getUsername());
        verifyAll();
    }

    @Test
    public void getUserStatusesIncludesSuperflyAdminWithRole() {
        expect(internal.isUserAccessibleFrom("admin", CALLER)).andReturn(true);
        UserWithStatus admin = new UserWithStatus();
        admin.setUserName("admin");
        expect(internal.getUserStatuses("admin")).andReturn(Collections.singletonList(admin));
        replayAll();
        List<UserStatus> result = ssoService.getUserStatuses(new GetUserStatusesRequest(Collections.singletonList("admin")));
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
        verifyAll();
    }

    @Test
    public void getUserStatusesNameWithCommaNeverReachesDao() {
        // "x,admin" is a legitimate-looking login of the caller's own user, but the DAO would split it
        expect(internal.isUserAccessibleFrom("own", CALLER)).andReturn(true);
        expect(internal.getUserStatuses("own")).andReturn(Collections.emptyList());
        replayAll();
        assertTrue(ssoService.getUserStatuses(new GetUserStatusesRequest(Arrays.asList("x,admin", "own"))).isEmpty());
        verifyAll();
    }

    @Test
    public void getUserStatusesDropsRowsOfUsersThatDidNotPassTheGuard() {
        expect(internal.isUserAccessibleFrom("own", CALLER)).andReturn(true);
        UserWithStatus own = new UserWithStatus();
        own.setUserName("OWN");
        UserWithStatus foreign = new UserWithStatus();
        foreign.setUserName("admin");
        expect(internal.getUserStatuses("own")).andReturn(Arrays.asList(own, foreign));
        replayAll();
        List<UserStatus> result = ssoService.getUserStatuses(new GetUserStatusesRequest(Collections.singletonList("own")));
        assertEquals(1, result.size());
        assertEquals("OWN", result.get(0).getUsername());
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void checkOtpForeignUserWithoutOtpTypeFailsLikeUnknownUser() throws Exception {
        expectForeign();
        replayAll();
        ssoService.checkOtp(new CheckOtpRequest(USER, "123456", null, false));
    }

    // no subsystem in the security context

    @Test
    public void callerWithoutSubsystemIsDeniedEverything() {
        ssoService.setSubsystemIdentifierObtainer(hint -> null);
        expect(internal.isUserAccessibleFrom(USER, null)).andReturn(false).times(2);
        replayAll();
        assertFalse(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest(USER)));
        assertNull(ssoService.getUserDescription(GetUserDescriptionRequest.builder().username(USER).build()));
        verifyAll();
    }

    @Test
    public void callerWithoutSubsystemCannotChangeAnything() throws Exception {
        ssoService.setSubsystemIdentifierObtainer(hint -> null);
        expect(internal.isUserManageableFrom(USER, null)).andReturn(false).times(2);
        replayAll();
        ssoService.completeUser(CompleteUserRequest.builder().username(USER).build());
        try {
            ssoService.resetPassword(resetRequest("pw"));
            fail();
        } catch (UserNotFoundException expected) {
            // same as for an unknown user
        }
        verifyAll();
    }
}
