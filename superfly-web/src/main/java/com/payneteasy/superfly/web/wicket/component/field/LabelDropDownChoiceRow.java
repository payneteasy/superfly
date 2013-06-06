package com.payneteasy.superfly.web.wicket.component.field;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

public class LabelDropDownChoiceRow<T> extends Panel {
	private final SuperflyDropDrownChoice<T> dropDownChoice;

	public LabelDropDownChoiceRow(String id, Object object, final String labelResourceKey, final List<? extends T> choices,
			IChoiceRenderer<? super T> iChoiceRenderer) {
		this(id, object, labelResourceKey, new AbstractReadOnlyModel<List<? extends T>>() {
			@Override
			public List<? extends T> getObject() {
				return choices;
			}
		}, iChoiceRenderer, false);
	}

	public LabelDropDownChoiceRow(String id, Object object, String labelResourceKey, IModel<? extends List<? extends T>> choices,
			IChoiceRenderer<? super T> iChoiceRenderer) {
		this(id, object, labelResourceKey, choices, iChoiceRenderer, false);
	}

	public LabelDropDownChoiceRow(String id, Object object, final String labelResourceKey, IModel<? extends List<? extends T>> choices,
			IChoiceRenderer<? super T> iChoiceRenderer, boolean nullValid) {
		this(id, new PropertyModel<T>(object, id), labelResourceKey, choices, iChoiceRenderer, nullValid);
	}

	public LabelDropDownChoiceRow(String id, IModel<T> object, String labelResourceKey, IModel<? extends List<? extends T>> choices,
			IChoiceRenderer<? super T> iChoiceRenderer) {
		this(id, object, labelResourceKey, choices, iChoiceRenderer, false);
	}

	public LabelDropDownChoiceRow(String id, IModel<T> object, final String labelResourceKey, IModel<? extends List<? extends T>> choices,
			IChoiceRenderer<? super T> iChoiceRenderer, boolean nullValid) {
		super(id);

		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		container.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return dropDownChoice.isRequired() ? "required" : "";
			}
		}, " "));

		// label
		Label label = new Label("label-id", new ResourceModel(labelResourceKey));
		label.add(new AttributeModifier("for", new Model<String>(id)));
		container.add(label);

		// field
		dropDownChoice = new SuperflyDropDrownChoice<T>("field-id", object, choices, iChoiceRenderer) {
			@Override
			public String getValidatorKeyPrefix() {
				return labelResourceKey;
			}
		};
		dropDownChoice.setNullValid(nullValid);
		dropDownChoice.setValidatorKeyPrefix(labelResourceKey);
		container.add(dropDownChoice);
	}

	public DropDownChoice<T> getDropDownChoice() {
		return dropDownChoice;
	}

	public void clearInput() {
		dropDownChoice.clearInput();
	}

	public void setEnabledDropDownChoice(boolean enabled) {
		dropDownChoice.setEnabled(enabled);
	}
}
