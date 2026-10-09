package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.web.wicket.SuperflyApplication;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * The SMTP password is shown only on explicit request, is not kept in the serialized pages and is not sent through
 * the edit form.
 */
public class SmtpServerPagesPasswordTest extends AbstractPageTest {
    private static final String PASSWORD = "fake-smtp-password";

    private SmtpServerService smtpServerService;
    private SettingsService   settingsService;
    private Capture<UISmtpServer> updated;

    @Before
    public void setUp() {
        tester.getApplication().getResourceSettings().getStringResourceLoaders()
                .add(new ClassStringResourceLoader(SuperflyApplication.class));
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));

        smtpServerService = createNiceMock(SmtpServerService.class);
        settingsService = createNiceMock(SettingsService.class);
        // as the service returns it for display: without the password
        expect(smtpServerService.getSmtpServer(1L)).andStubAnswer(() -> server(null));
        expect(smtpServerService.getSmtpServerWithPassword(1L)).andStubAnswer(() -> server(PASSWORD));
        updated = newCapture();
        expect(smtpServerService.updateSmtpServer(capture(updated))).andStubAnswer(() -> {
            // the service clears the password in the model after storing it
            ((UISmtpServer) getCurrentArguments()[0]).setPassword(null);
            return RoutineResult.okResult();
        });
        expect(settingsService.getSuperflyVersion()).andStubReturn("test");
        smtpServerService.logPasswordViewed("main");
        expectLastCall().once();
        replay(smtpServerService, settingsService);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == SmtpServerService.class) {
            return smtpServerService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        return super.getBean(type);
    }

    @Test
    public void viewPageShowsPasswordOnlyOnRequestAndDoesNotKeepIt() throws Exception {
        tester.startPage(ViewSmtpServerPage.class, new PageParameters().add("id", 1));
        assertFalse(tester.getLastResponseAsString().contains(PASSWORD));

        tester.clickLink("password", true);

        assertTrue(tester.getLastResponseAsString().contains(PASSWORD));
        verify(smtpServerService);
        byte[] serialized = serialize(tester.getLastRenderedPage());
        assertTrue(serialized.length > 0);
        assertFalse(new String(serialized, StandardCharsets.ISO_8859_1).contains(PASSWORD));
    }

    @Test
    public void editWithEmptyPasswordSendsNoPassword() {
        tester.startPage(UpdateSmtpServerPage.class, new PageParameters().add("id", 1));

        FormTester form = tester.newFormTester("create-edit-panel:form");
        form.setValue("host:row:field-id", "smtp2.example.com");
        form.submit();

        assertTrue(updated.hasCaptured());
        assertEquals("smtp2.example.com", updated.getValue().getHost());
        assertNull(updated.getValue().getPassword());
    }

    @Test
    public void editWithNewPasswordSendsIt() {
        tester.startPage(UpdateSmtpServerPage.class, new PageParameters().add("id", 1));

        FormTester form = tester.newFormTester("create-edit-panel:form");
        form.setValue("password:row:field-id", "new-fake-password");
        Capture<String> sent = newCapture();
        reset(smtpServerService);
        expect(smtpServerService.updateSmtpServer(anyObject())).andAnswer(() -> {
            sent.setValue(((UISmtpServer) getCurrentArguments()[0]).getPassword());
            return RoutineResult.okResult();
        });
        expect(smtpServerService.getSmtpServer(1L)).andStubAnswer(() -> server(null));
        replay(smtpServerService);
        form.submit();

        assertEquals("new-fake-password", sent.getValue());
    }

    private static UISmtpServer server(String password) {
        UISmtpServer server = new UISmtpServer();
        server.setId(1L);
        server.setName("main");
        server.setHost("smtp.example.com");
        server.setPort(25);
        server.setUsername("mailer");
        server.setFrom("noreply@example.com");
        server.setPassword(password);
        return server;
    }

    // plain Java serialization; the injected test mocks are not serializable, so they are skipped
    private static byte[] serialize(Object page) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes) {
            {
                enableReplaceObject(true);
            }

            @Override
            protected Object replaceObject(Object obj) {
                return obj instanceof Serializable ? obj : null;
            }
        }) {
            out.writeObject(page);
        }
        return bytes.toByteArray();
    }
}
