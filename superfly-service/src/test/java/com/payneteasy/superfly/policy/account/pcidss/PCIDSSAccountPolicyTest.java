package com.payneteasy.superfly.policy.account.pcidss;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PasswordGeneratorImpl;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PCIDSSAccountPolicyTest {
    private PCIDSSAccountPolicy policy;
    private UserService userService;

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        PasswordGeneratorImpl passwordGenerator = new PasswordGeneratorImpl();
        policy = new PCIDSSAccountPolicy();
        policy.setPasswordGenerator(passwordGenerator);
        UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
        userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        userPasswordEncoder.setSaltSource(new NullSaltSource());
        policy.setUserPasswordEncoder(userPasswordEncoder);
        policy.setUserService(userService);
    }

    @Test
    public void testUnlockNotSuspendedUser() {
        EasyMock.expect(userService.unlockUser(1L)).andReturn(RoutineResult.okResult());
        EasyMock.replay(userService);

        Assert.assertNull(policy.unlockUser(1L, false));

        EasyMock.verify(userService);
    }

    @Test
    public void testUnlockSuspendedUser() {
        EasyMock.expect(userService.unlockSuspendedUser(EasyMock.eq(1L), EasyMock.anyObject(String.class))).andReturn(RoutineResult.okResult());
        EasyMock.replay(userService);

        Assert.assertNotNull(policy.unlockUser(1L, true));

        EasyMock.verify(userService);
    }
}
