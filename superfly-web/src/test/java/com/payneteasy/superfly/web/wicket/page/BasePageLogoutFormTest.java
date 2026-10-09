package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.SuperflyApplication;
import com.payneteasy.superfly.web.wicket.page.subsystem.AddSubsystemPage;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Logout must be a POST form: Spring Security no longer accepts GET on the logout URL.
 */
public class BasePageLogoutFormTest extends AbstractPageTest {
    private SubsystemService  subsystemService;
    private SmtpServerService smtpServerService;
    private SettingsService   settingsService;

    @Before
    public void setUp() {
        tester.getApplication().getResourceSettings().getStringResourceLoaders()
                .add(new ClassStringResourceLoader(SuperflyApplication.class));
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
        subsystemService = createNiceMock(SubsystemService.class);
        smtpServerService = createNiceMock(SmtpServerService.class);
        settingsService = createNiceMock(SettingsService.class);
        expect(smtpServerService.getSmtpServersForFilter()).andStubReturn(Collections.emptyList());
        expect(settingsService.getSuperflyVersion()).andStubReturn("test");
        replay(subsystemService, smtpServerService, settingsService);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == SmtpServerService.class) {
            return smtpServerService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        return super.getBean(type);
    }

    @Test
    public void logoutIsPostFormNotLink() {
        tester.startPage(AddSubsystemPage.class);

        String html = tester.getLastResponseAsString();
        assertTrue(html, html.matches("(?s).*<form[^>]*method=\"post\"[^>]*action=\"[^\"]*j_spring_security_logout\".*"));
        assertFalse(html, html.matches("(?s).*<a[^>]*j_spring_security_logout.*"));
    }
}
