package com.payneteasy.superfly.web.wicket.component.field;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.validator.UrlValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Validation messages must name the field by its label, not by the internal wicket id "field-id".
 */
public class LabelTextFieldRowTest {

    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new MockApplication());
        tester.getApplication().getResourceSettings().getStringResourceLoaders().add(0, new IStringResourceLoader() {
            @Override
            public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation) {
                return "subsystem.edit.subsystemUrl".equals(key) ? "Subsystem URL" : null;
            }

            @Override
            public String loadStringResource(Component component, String key, Locale locale, String style,
                                             String variation) {
                return loadStringResource((Class<?>) null, key, locale, style, variation);
            }
        });
    }

    @After
    public void tearDown() {
        tester.destroy();
    }

    @Test
    public void urlValidationMessageUsesFieldLabel() {
        FormPanel panel = new FormPanel("panel");
        tester.startComponentInPage(panel);

        FormTester formTester = tester.newFormTester("panel:form");
        formTester.setValue("subsystemUrl:row:field-id", "not a url");
        formTester.submit();

        String message = tester.getMessages(org.apache.wicket.feedback.FeedbackMessage.ERROR).toString();
        assertTrue(message, message.contains("Subsystem URL"));
        assertFalse(message, message.contains("field-id"));
    }

    private static class FormPanel extends org.apache.wicket.markup.html.panel.Panel implements IMarkupResourceStreamProvider {
        FormPanel(String id) {
            super(id);
            Form<Void> form = new Form<>("form");
            LabelTextFieldRow<String> row = new LabelTextFieldRow<>("subsystemUrl", Model.of(""),
                    "subsystem.edit.subsystemUrl", true);
            row.getTextField().add(new UrlValidator(new String[]{"http", "https"}));
            form.add(row);
            add(form);
        }

        @Override
        public IResourceStream getMarkupResourceStream(org.apache.wicket.MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<wicket:panel><form wicket:id=\"form\"><div wicket:id=\"subsystemUrl\"></div></form></wicket:panel>");
        }
    }
}
