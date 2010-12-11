package com.payneteasy.superfly.web.wicket.page.action;

import java.io.Serializable;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.service.ActionService;

public class CopyActionWindow extends WebPage {
	@SpringBean
	private ActionService actionService;

	@SuppressWarnings("deprecation")
	public CopyActionWindow(final CopyActionPropertiesPage actionPropertiesPage, final ModalWindow modalWindow,
			final PageParameters parameters) {
		super(parameters);
		final Long actionId = parameters.getAsLong("id");
		final Long actionIdForCopy = parameters.getAsLong("copyId");
		UIAction action = actionService.getAction(actionId);
		UIAction actionForCopy = actionService.getAction(actionIdForCopy);
		final ActionSelected check = new ActionSelected();
		Form form = new Form("form") {

		};
		add(form);
		form.add(new Label("action-name-for-copy", actionForCopy.getActionName()));
		form.add(new Label("action-desc-for-copy", actionForCopy.getActionDescription()));
		form.add(new Label("action-subname-for-copy", actionForCopy.getSubsystemName()));

		form.add(new Label("action-name", action.getActionName()));
		form.add(new Label("action-desc", action.getActionDescription()));
		form.add(new Label("action-subname", action.getSubsystemName()));
		form.add(new CheckBox("selected", new PropertyModel<Boolean>(check, "selected")));
		form.add(new AjaxSubmitButton("copy") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				actionService.copyActionProperties(actionIdForCopy, actionId, check.isSelected() ? true : false);
				info("properties are copied");
				parameters.put("copy", "copy");
				modalWindow.close(target);
			}

		});
		form.add(new AjaxSubmitButton("cancel") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				modalWindow.close(target);

			}

		});

	}

	@SuppressWarnings("unused")
	private class ActionSelected implements Serializable {
		private boolean selected;

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

	}
}
