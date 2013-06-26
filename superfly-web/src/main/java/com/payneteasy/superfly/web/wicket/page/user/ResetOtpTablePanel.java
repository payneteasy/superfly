package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * @author rpuch
 */
public class ResetOtpTablePanel extends Panel {
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private HOTPService hotpService;

    public ResetOtpTablePanel(String id, final long userId,
            final ModalWindow window, final FeedbackPanel feedbackPanel) {
        super(id);
        
        final FeedbackPanel feedbackPanel2 = new FeedbackPanel("feedback");
        feedbackPanel2.setOutputMarkupId(true);
        add(feedbackPanel2);

        Form<?> form = new Form<Void>("form");
        add(form);

        final IModel<UISubsystemForFilter> subsystemModel = new Model<UISubsystemForFilter>();

        DropDownChoice<UISubsystemForFilter> subsystemChoice = new DropDownChoice<UISubsystemForFilter>("subsystem",
                subsystemModel,
                subsystemService.getSubsystemsForFilter(),
                new SubsystemChoiceRenderer());
        subsystemChoice.setRequired(true);
		form.add(subsystemChoice);

        form.add(new AjaxLink<Void>("cancel-link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                window.close(target);
            }
        });

        form.add(new AjaxSubmitLink("submit-link") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    hotpService.resetTableAndSendIfSupported(subsystemModel.getObject().getName(), userId);
                    info("Done");
                    target.add(feedbackPanel);
                } catch (MessageSendException e) {
                    error("Could not send a message: " + e.getMessage());
                    target.add(feedbackPanel);
                }
                window.close(target);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            	target.add(feedbackPanel2);
            }
        });
    }
}
