package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.markup.repeater.Item;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Date;

import static org.easymock.EasyMock.*;

public class ListUsersPageTest extends AbstractPageTest {
    private static final String ROW = "usersList:1:";

    private UserService userService;
    private RoleService roleService;
    private SubsystemService subsystemService;
    private SettingsService settingsService;
    private HOTPProvider hotpProvider;

    @Before
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));

        userService = createNiceMock(UserService.class);
        roleService = createNiceMock(RoleService.class);
        subsystemService = createNiceMock(SubsystemService.class);
        settingsService = createNiceMock(SettingsService.class);
        hotpProvider = createNiceMock(HOTPProvider.class);

        UIUserForList user = new UIUserForList();
        user.setId(1L);
        user.setUsername("bond");
        user.setEmail("bond@example.com");
        user.setLastLoginDate(new Date());

        expect(roleService.getRolesForFilter()).andStubReturn(Collections.emptyList());
        expect(subsystemService.getSubsystemsForFilter()).andStubReturn(Collections.emptyList());
        expect(settingsService.getSuperflyVersion()).andStubReturn("test");
        expect(userService.getUsers(anyObject(), anyObject(), anyObject(), anyObject(),
                anyLong(), anyLong(), anyInt(), anyBoolean())).andStubReturn(Collections.singletonList(user));
        expect(userService.getUsersCount(anyObject(), anyObject(), anyObject(), anyObject())).andStubReturn(1L);
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
        if (type == HOTPProvider.class) {
            return hotpProvider;
        }
        return super.getBean(type);
    }

    @Test
    public void testHotpTableLinksVisibleWhenProviderOutputsSequence() {
        expect(hotpProvider.outputsSequenceForDownload()).andStubReturn(true);
        replay(userService, roleService, subsystemService, settingsService, hotpProvider);

        tester.startPage(ListUsersPage.class);

        tester.assertRenderedPage(ListUsersPage.class);
        tester.assertVisible(ROW + "reset-table-link");
        tester.assertVisible(ROW + "download-hotp-table");
    }

    @Test
    public void testHotpTableLinksHiddenWhenProviderDoesNotOutputSequence() {
        expect(hotpProvider.outputsSequenceForDownload()).andStubReturn(false);
        replay(userService, roleService, subsystemService, settingsService, hotpProvider);

        tester.startPage(ListUsersPage.class);

        tester.assertRenderedPage(ListUsersPage.class);
        tester.assertInvisible(ROW + "reset-table-link");
        tester.assertInvisible(ROW + "download-hotp-table");
    }
}
