package com.payneteasy.superfly.web.wicket.component.field;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

/**
 * Row with check box and label.
 */
public class LabelCheckBoxRow extends Panel {
    
    private CheckBox checkBox;
    
    public LabelCheckBoxRow(String id, Object object, String labelResourceKey) {
        super(id);

        // label
        Label label = new Label("label-id", new ResourceModel(labelResourceKey));
        label.add(new AttributeModifier("for", true, new Model<String>(id)));
        add(label);

        // field
        checkBox = new LabeledCheckBox("field-id", new PropertyModel<Boolean>(object, id), id);
        add(checkBox);
    }
    
    public CheckBox getCheckBox() {
        return checkBox;
    }

    private class LabeledCheckBox extends CheckBox {
        
        public LabeledCheckBox(String id, IModel<Boolean> model, String markupId) {
            super(id, model);
            setOutputMarkupId(true);
            setMarkupId(markupId);
        }
    }
}

