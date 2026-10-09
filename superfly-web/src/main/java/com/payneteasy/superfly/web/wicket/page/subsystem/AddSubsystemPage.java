package com.payneteasy.superfly.web.wicket.page.subsystem;

import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.impl.remote.check.KeyPairData;
import com.payneteasy.superfly.service.impl.remote.check.RemoteAuthEncryptionAlgorithm;
import com.payneteasy.superfly.web.wicket.component.field.LabelCheckBoxRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@Secured("ROLE_ADMIN")
public class AddSubsystemPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(AddSubsystemPage.class);

    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SmtpServerService smtpServerService;

    public AddSubsystemPage() {
        super(ListSubsystemsPage.class);

        final UISubsystem subsystem = new UISubsystem();
        // the raw token is kept only for the response that shows it: not in the page store, not in session feedback
        final OneTimeModel newTokenModel = new OneTimeModel();
        final boolean[] created = {false};

        final WebMarkupContainer createdBlock = new WebMarkupContainer("created") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(created[0]);
            }
        };
        createdBlock.add(new Label("newSubsystemToken", newTokenModel));
        createdBlock.add(new BookmarkablePageLink<Page>("back", ListSubsystemsPage.class));
        add(createdBlock);

        Form<UISubsystem> form = new Form<UISubsystem>("form", new CompoundPropertyModel<>(subsystem)) {
            @Override
            protected void onSubmit() {
                String token = subsystemService.generateMainSubsystemToken(subsystem);
                RoutineResult result = subsystemService.createSubsystem(subsystem);
                if (result.isOk()) {
                    newTokenModel.setObject("Subsystem token (shown once, store it now): " + token);
                    created[0] = true;
                } else {
                    setResponsePage(ListSubsystemsPage.class);
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!created[0]);
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
        loginFormCssUrlRow.getTextField().add(new UrlValidator(new String[]{"https"}));
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

        final Label labelPublicKey = new Label("publicKey", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return subsystem.getPublicKey();
            }
        });
        labelPublicKey.setOutputMarkupId(true);

        form.add(new Label("publicKeyLabel", new ResourceModel("subsystem.add.publicKey")));
        form.add(labelPublicKey);
        form.add(new IndicatingAjaxLink<String>("generateNewKeyPair") {
            private static final long serialVersionUID = 1L;

            public void onClick(AjaxRequestTarget aTarget) {
                try {
                    var keyPairData = generateKeyPair(RemoteAuthEncryptionAlgorithm.RSA_OAEP);
                    subsystem.setPrivateKey(keyPairData.privateKey());
                    subsystem.setPublicKey(keyPairData.publicKey());
                    subsystem.setEncryptionAlgorithm(RemoteAuthEncryptionAlgorithm.RSA_OAEP.name());
                    aTarget.add(labelPublicKey);
                } catch (Exception e) {
                    logger.error("Error while generating key pair for subsystem {}", subsystem.getName(), e);
                    error("Error while generating key pair: " + e.getMessage());
                }

            }
        });

        form.add(new SubmitLink("submit-link"));
        form.add(new BookmarkablePageLink<Page>("cancel", ListSubsystemsPage.class));
    }

    @Override
    protected String getTitle() {
        return "Add subsystem";
    }

    /**
     * Holds the raw token for the current request only: cleared on detach and never serialized.
     */
    private static class OneTimeModel implements IModel<String> {
        private transient String value;

        @Override
        public String getObject() {
            return value;
        }

        @Override
        public void setObject(String object) {
            value = object;
        }

        @Override
        public void detach() {
            value = null;
        }
    }

    private KeyPairData generateKeyPair(RemoteAuthEncryptionAlgorithm algorithm) {
        return subsystemService.generateKeyPair(algorithm);
    }
}
