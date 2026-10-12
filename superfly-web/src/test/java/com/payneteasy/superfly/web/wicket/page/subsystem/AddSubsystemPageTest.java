package com.payneteasy.superfly.web.wicket.page.subsystem;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.web.wicket.SuperflyApplication;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.util.tester.FormTester;
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
 * After creation the raw token is shown once on the page itself: not in session feedback, not in the page store.
 */
public class AddSubsystemPageTest extends AbstractPageTest {
    private static final String RAW = "raw-token-for-the-add-test";

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
        expect(subsystemService.generateMainSubsystemToken(anyObject(UISubsystem.class))).andStubAnswer(() -> {
            ((UISubsystem) getCurrentArguments()[0]).setSubsystemToken(SubsystemTokenHasher.hash(RAW));
            return RAW;
        });
        expect(subsystemService.createSubsystem(anyObject(UISubsystem.class))).andStubReturn(RoutineResult.okResult());
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
    public void createdTokenIsShownOnceAndNotKept() throws Exception {
        tester.startPage(AddSubsystemPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("name:row:field-id", "billing");
        form.setValue("title:row:field-id", "Billing");
        form.setValue("callbackUrl:row:field-id", "http://localhost/cb");
        form.setValue("subsystemUrl:row:field-id", "http://localhost/");
        form.setValue("landingUrl:row:field-id", "http://localhost/landing");
        form.submit();

        assertTrue(tester.getLastResponseAsString().contains(RAW));
        assertTrue(tester.getSession().getFeedbackMessages().isEmpty());

        byte[] serialized = serialize(tester.getLastRenderedPage());
        assertFalse(new String(serialized, StandardCharsets.ISO_8859_1).contains(RAW));

        tester.startPage(tester.getLastRenderedPage());
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
