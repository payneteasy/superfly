package com.payneteasy.superfly.web.wicket.component.field;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

public class LabelTextAreaRow<T> extends Panel {
	private final TextArea<T> textField;

	public LabelTextAreaRow(Object aModelObject, String aProperty, String aResourceKey) {
		this(aModelObject, aProperty, aResourceKey, false);
	}

	public LabelTextAreaRow(Object aModelObject, String aProperty, String aResourceKey, boolean aRequired) {
		super(aProperty);

		// row
		WebMarkupContainer row = new WebMarkupContainer("row");
		row.add(new AttributeModifier("class", true, new Model<String>(aRequired ? "text-field-required" : "text-field")));
		add(row);

		// label
		Label label = new Label("label-id", new ResourceModel(aResourceKey));
		label.add(new AttributeModifier("for", true, new Model<String>(aProperty)));
		row.add(label);

		// field
		row.add(textField = new LabeledTextAreaField<T>("field-id", new PropertyModel<T>(aModelObject, aProperty), aProperty, aResourceKey,
				aRequired));
	}

	public TextArea<T> getTextField() {
		return textField;
	}

	private class LabeledTextAreaField<E> extends TextArea<E> {
		public LabeledTextAreaField(String aId, IModel<E> aModel, String aMarkupId, String aResourceKey, boolean aRequired) {
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