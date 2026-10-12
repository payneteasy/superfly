package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.UserService;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;

/**
 * Accounts for the security filter chain tests that are not about the account status: every user exists and
 * is active, so a logged-in session stays valid.
 */
public final class ActiveAccounts {

    private static final long USER_ID = 1;

    private ActiveAccounts() {
    }

    public static UserService userService() {
        UserForDescription description = new UserForDescription();
        description.setUserId(USER_ID);
        UserService userService = niceMock(UserService.class);
        expect(userService.getUserForDescription(anyString())).andStubReturn(description);
        expect(userService.getUser(USER_ID)).andStubReturn(new UIUserDetails());
        replay(userService);
        return userService;
    }
}
