package com.payneteasy.superfly.web.wicket.component.field;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;


public class LabelPasswordTextFieldRow extends Panel{
	private final PasswordTextField passwordTextField;

	public LabelPasswordTextFieldRow(Object aModelObject, String aProperty, String aResourceKey) {
		this(aModelObject, aProperty, aResourceKey, false);
	}

	public LabelPasswordTextFieldRow(Object aModelObject, String aProperty, String aResourceKey, boolean aRequired) {
		this(aProperty, new PropertyModel<String>(aModelObject, aProperty), aResourceKey, aRequired);
	}

	public LabelPasswordTextFieldRow(String aId, IModel<String> aModel, String aResourceKey, boolean aRequired) {
		super(aId);

		// row
		WebMarkupContainer row = new WebMarkupContainer("row");
		row.add(new AttributeAppender("class", new Model<String>(aRequired ? "required" : ""), " "));
		add(row);

		// label
		Label label = new Label("label-id", new ResourceModel(aResourceKey));
		label.add(new AttributeModifier("for", new Model<String>(aId)));
		row.add(label);

		// field
		row.add(passwordTextField = new LabeledRequiredTextField("field-id", aModel, aId, aResourceKey, aRequired));
	}

	public PasswordTextField getPasswordTextField() {
		return passwordTextField;
	}

	private class LabeledRequiredTextField extends PasswordTextField {
		public LabeledRequiredTextField(String aId, IModel<String> aModel, String aMarkupId, String aResourceKey, boolean aRequired) {
			super(aId, aModel);
			theResourceKey = aResourceKey;
			setRequired(aRequired);
			setOutputMarkupId(true);
			setMarkupId(aMarkupId);
		}

		@Override
		public String getValidatorKeyPrefix() {
			return theResourceKey;
		}

		private final String theResourceKey;
	}

}
