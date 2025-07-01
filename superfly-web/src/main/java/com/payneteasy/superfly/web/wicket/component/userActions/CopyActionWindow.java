package com.payneteasy.superfly.web.wicket.component.userActions;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.service.ActionService;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serializable;

public class CopyActionWindow extends Panel {
    @SpringBean
    private ActionService actionService;

    public CopyActionWindow(
            final ModalDialog modal,
            final Long actionId,
            Long actionIdForCopy,
            FeedbackPanel feedbackPanel
    ) {
        super(ModalDialog.CONTENT_ID);
        UIAction             action        = actionService.getAction(actionId);
        UIAction             actionForCopy = actionService.getAction(actionIdForCopy);
        final ActionSelected check         = new ActionSelected();
        Form<Void>           form          = new Form<>("form");
        add(form);
        form.add(new Label("action-name-for-copy", actionForCopy.getActionName()));
        form.add(new Label("action-desc-for-copy", actionForCopy.getActionDescription()));
        form.add(new Label("action-subname-for-copy", actionForCopy.getSubsystemName()));

        form.add(new Label("action-name", action.getActionName()));
        form.add(new Label("action-desc", action.getActionDescription()));
        form.add(new Label("action-subname", action.getSubsystemName()));
        form.add(new CheckBox("selected", new PropertyModel<>(check, "selected")));
        form.add(new AjaxButton("copy") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                RoutineResult routineResult = actionService.copyActionProperties(
                        actionId,
                        actionIdForCopy,
                        check.isSelected()
                );
                if (routineResult.isOk()) {
                    feedbackPanel.info("properties are copied");
                } else {
                    feedbackPanel.error(routineResult.getErrorMessage());
                }
                target.add(feedbackPanel);
                modal.close(target);
            }

        });
        form.add(new AjaxButton("cancel") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                modal.close(target);
            }
        });
    }

    @Setter
    @Getter
    @SuppressWarnings("unused")
    private static class ActionSelected implements Serializable {
        private boolean selected;
    }
}
