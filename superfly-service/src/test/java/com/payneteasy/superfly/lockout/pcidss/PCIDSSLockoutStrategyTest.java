package com.payneteasy.superfly.lockout.pcidss;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.service.UserService;
import org.junit.Test;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class PCIDSSLockoutStrategyTest {

    private void check(LockoutType type, long expectedMax) {
        UserService userService = createStrictMock(UserService.class);
        expect(userService.lockoutConditionnally("user", expectedMax, type.name())).andReturn(null);
        replay(userService);

        SuperflyProperties properties = new SuperflyProperties().maxLoginsFailed(6L).maxOtpFailed(3L);
        new PCIDSSLockoutStrategy(userService, properties).checkLoginsFailed("user", type);

        verify(userService);
    }

    @Test
    public void otpUsesOtpThreshold() {
        check(LockoutType.HOTP, 3L);
    }

    @Test
    public void passwordUsesLoginsThreshold() {
        check(LockoutType.PASSWORD, 6L);
    }
}
