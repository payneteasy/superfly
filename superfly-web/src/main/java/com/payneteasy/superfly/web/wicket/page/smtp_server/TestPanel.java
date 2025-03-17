package com.payneteasy.superfly.web.wicket.page.smtp_server;

import com.payneteasy.superfly.email.EmailService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

@Slf4j
public class TestPanel extends Panel {

    @SpringBean
    private EmailService emailService;

    @Getter
    private RequiredTextField<String> addressField;

    public TestPanel(String id, final long serverId, final ModalDialog window,
                     final FeedbackPanel feedbackPanel) {
        super(id);

        Form<?> form = new Form<Void>("form");
        add(form);

        final IModel<String> addressModel = new Model<String>();
        addressField = new RequiredTextField<>("address", addressModel);
        addressField.setOutputMarkupId(true);
        form.add(addressField);

        form.add(new AjaxLink<Void>("cancel-link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                window.close(target);
            }
        });
        form.add(new AjaxSubmitLink("submit-link") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    emailService.sendTestMessage(serverId, addressModel.getObject());
                    info("Test message sent");
                    target.add(feedbackPanel);
                } catch (Exception e) {
                    log.error("Can't sendTestMessage", e);
                    error(e.getMessage() != null ? e.getMessage() : "Some problem, see log");
                    target.add(feedbackPanel);
                }
                window.close(target);
            }

            public void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
            }
        });
    }

}
