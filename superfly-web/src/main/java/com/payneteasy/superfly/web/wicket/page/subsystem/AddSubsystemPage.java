package com.payneteasy.superfly.web.wicket.page.subsystem;

import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.field.LabelCheckBoxRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@Secured("ROLE_ADMIN")
public class AddSubsystemPage extends BasePage {
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SmtpServerService smtpServerService;

    public AddSubsystemPage() {
        super(ListSubsystemsPage.class);

        final UISubsystem subsystem = new UISubsystem();
        Form<UISubsystem> form = new Form<UISubsystem>("form", new CompoundPropertyModel<>(subsystem)) {
            @Override
            protected void onSubmit() {
                subsystemService.createSubsystem(subsystem);
                setResponsePage(ListSubsystemsPage.class);
            }

        };
        add(form);
        form.add(new LabelTextFieldRow<UISubsystem>(subsystem, "name", "subsystem.add.name", true));
        form.add(new LabelTextFieldRow<UISubsystem>(subsystem, "title", "subsystem.add.title", true));

        LabelTextFieldRow<String> callbackUrlRow = new LabelTextFieldRow<>(subsystem, "callbackUrl",
                "subsystem.add.callback", true);
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        callbackUrlRow.getTextField().add(urlValidator);
        form.add(callbackUrlRow);

        form.add(new LabelCheckBoxRow("sendCallbacks", subsystem, "subsystem.add.send-callbacks"));

        LabelTextFieldRow<String> subsystemUrlRow = new LabelTextFieldRow<>(subsystem, "subsystemUrl",
                "subsystem.add.subsystemUrl", true);
        subsystemUrlRow.getTextField().add(urlValidator);
        form.add(subsystemUrlRow);

        LabelTextFieldRow<String> landingUrlRow = new LabelTextFieldRow<>(subsystem, "landingUrl",
                "subsystem.add.landingUrl", true);
        landingUrlRow.getTextField().add(urlValidator);
        form.add(landingUrlRow);

        LabelTextFieldRow<String> loginFormCssUrlRow = new LabelTextFieldRow<>(subsystem, "loginFormCssUrl",
                "subsystem.add.loginFormCssUrl");
        loginFormCssUrlRow.getTextField().add(urlValidator);
        form.add(loginFormCssUrlRow);

        form.add(new LabelCheckBoxRow("allowListUsers", subsystem, "subsystem.add.allow-list-users"));

        IModel<List<UISmtpServerForFilter>> smtpServersModel = new LoadableDetachableModel<List<UISmtpServerForFilter>>() {
            @Override
            protected List<UISmtpServerForFilter> load() {
                return smtpServerService.getSmtpServersForFilter();
            }
        };
        form.add(new LabelDropDownChoiceRow<>("smtpServer", subsystem, "subsystem.smtpServer",
                smtpServersModel, new ChoiceRenderer<UISmtpServerForFilter>() {
            public Object getDisplayValue(UISmtpServerForFilter server) {
                return server == null ? "" : server.getName();
            }

            public String getIdValue(UISmtpServerForFilter server, int index) {
                return server == null ? "" : String.valueOf(server.getId());
            }
        }, true));

        form.add(new SubmitLink("submit-link"));
        form.add(new BookmarkablePageLink<Page>("cancel", ListSubsystemsPage.class));
    }

    @Override
    protected String getTitle() {
        return "Add subsystem";
    }
}
