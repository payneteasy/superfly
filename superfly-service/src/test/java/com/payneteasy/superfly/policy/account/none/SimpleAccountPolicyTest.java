package com.payneteasy.superfly.policy.account.none;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleAccountPolicyTest {
    private SimpleAccountPolicy policy;
    private UserService userService;

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        policy = new SimpleAccountPolicy();
        policy.setUserService(userService);
    }

    @Test
    public void testUnlockUser() {
        EasyMock.expect(userService.unlockUser(1L)).andReturn(RoutineResult.okResult());
        EasyMock.replay(userService);

        String newPassword = policy.unlockUser(1L, true);
        Assert.assertNull(newPassword);

        EasyMock.verify(userService);
    }
}
