package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author rpuch
 */
public class SSOLogoutPageTest extends AbstractPageTest {
    private SessionService sessionService;
    private SubsystemService subsystemService;

    @Before
    public void setUp() {
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (SessionService.class == type) {
            return sessionService;
        }
        if (SubsystemService.class == type) {
            return subsystemService;
        }
        return super.getBean(type);
    }

    @Test
    public void testNoSSOCookie() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test");
        subsystem.setSubsystemUrl("http://some.domain.test/return-url");
        expect(subsystemService.getSubsystemByName("test")).andReturn(subsystem);
        replay(subsystemService);
        tester.startPage(SSOLogoutPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test");
        }}));
        tester.assertRedirectUrl("http://some.domain.test/return-url");
        verify(subsystemService);
    }

    @Test
    public void testWithSSOCookie() {
        sessionService.deleteSSOSession("super-session-id");
        EasyMock.expectLastCall();
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test");
        subsystem.setSubsystemUrl("http://some.domain.test/return-url");
        expect(subsystemService.getSubsystemByName("test")).andReturn(subsystem);

        replay(sessionService, subsystemService);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLogoutPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test");
        }}));
        tester.assertRedirectUrl("http://some.domain.test/return-url");

        verify(sessionService, subsystemService);
    }
}
