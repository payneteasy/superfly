package com.payneteasy.superfly.web.wicket.page.subsystem;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.web.wicket.SuperflyApplication;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The stored token is a hash and is never shown; a newly generated raw token is shown once and does not stay in the
 * page that is serialized into the page store.
 */
public class EditSubsystemPageTest extends AbstractPageTest {
    private static final String RAW = "raw-token-for-the-test";

    private SubsystemService  subsystemService;
    private SmtpServerService smtpServerService;
    private SettingsService   settingsService;
    private String            storedHash;

    @Before
    public void setUp() {
        tester.getApplication().getResourceSettings().getStringResourceLoaders()
                .add(new ClassStringResourceLoader(SuperflyApplication.class));
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));

        storedHash = SubsystemTokenHasher.hash("old-token");
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("billing");
        // ui_get_subsystem never returns the token, only the flag
        subsystem.setSubsystemTokenSet(true);

        subsystemService = createNiceMock(SubsystemService.class);
        smtpServerService = createNiceMock(SmtpServerService.class);
        settingsService = createNiceMock(SettingsService.class);
        expect(subsystemService.getSubsystem(1L)).andStubReturn(subsystem);
        UISubsystem without = new UISubsystem();
        without.setId(2L);
        without.setName("crm");
        expect(subsystemService.getSubsystem(2L)).andStubReturn(without);
        expect(subsystemService.generateMainSubsystemToken(anyObject(UISubsystem.class))).andStubAnswer(() -> {
            ((UISubsystem) getCurrentArguments()[0]).setSubsystemToken(SubsystemTokenHasher.hash(RAW));
            return RAW;
        });
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
    public void tokenPresenceIsShownByTheFlag() {
        tester.startPage(EditSubsystemPage.class, new PageParameters().add("id", 1));

        tester.assertLabel("form:subsystemToken", "Set");
    }

    @Test
    public void missingTokenIsShownAsNotSet() {
        tester.startPage(EditSubsystemPage.class, new PageParameters().add("id", 2));

        tester.assertLabel("form:subsystemToken", "Not set");
    }

    @Test
    public void storedTokenIsNotShown() {
        tester.startPage(EditSubsystemPage.class, new PageParameters().add("id", 1));

        String html = tester.getLastResponseAsString();
        assertFalse(html.contains(storedHash));
        assertFalse(html.contains(storedHash.substring(SubsystemTokenHasher.PREFIX.length())));
    }

    @Test
    public void generatedTokenIsShownOnceAndNotKeptInThePage() throws Exception {
        tester.startPage(EditSubsystemPage.class, new PageParameters().add("id", 1));

        tester.clickLink("form:generateNewToken", true);
        assertTrue(tester.getLastResponseAsString().contains(RAW));

        EditSubsystemPage page = (EditSubsystemPage) tester.getLastRenderedPage();
        byte[] serialized = serialize(page);
        assertTrue(serialized.length > 0);
        assertFalse(new String(serialized, StandardCharsets.ISO_8859_1).contains(RAW));

        tester.startPage(EditSubsystemPage.class, new PageParameters().add("id", 1));
        assertFalse(tester.getLastResponseAsString().contains(RAW));
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
