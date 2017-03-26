package com.payneteasy.superfly.web.wicket.component.field;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

public class LabelTextFieldRow<T> extends Panel {
    private final TextField<T> textField;

    public LabelTextFieldRow(Object aModelObject, String aProperty, String aResourceKey) {
        this(aModelObject, aProperty, aResourceKey, false);
    }

    public LabelTextFieldRow(Object aModelObject, String aProperty, String aResourceKey, boolean aRequired) {
        this(aProperty, new PropertyModel<T>(aModelObject, aProperty), aResourceKey, aRequired);
    }

    public LabelTextFieldRow(String aId, IModel<T> aModel, String aResourceKey, boolean aRequired) {
        super(aId);

        // row
        WebMarkupContainer row = new WebMarkupContainer("row");
        row.add(new AttributeAppender("class", new Model<String>(aRequired ? "required" : ""), " " ));
        add(row);

        // label
        Label label = new Label("label-id", new ResourceModel(aResourceKey));
        label.add(new AttributeModifier("for", new Model<String>(aId)));
        row.add(label);

        // field
        row.add(textField = new LabeledRequiredTextField<T>("field-id", aModel, aId, aResourceKey, aRequired));
    }

    public TextField<T> getTextField() {
        return textField;
    }

    private class LabeledRequiredTextField<E> extends TextField<E> {
        public LabeledRequiredTextField(String aId, IModel<E> aModel, String aMarkupId, String aResourceKey, boolean aRequired) {
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
