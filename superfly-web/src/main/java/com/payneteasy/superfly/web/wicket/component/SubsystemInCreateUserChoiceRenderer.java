package com.payneteasy.superfly.web.wicket.component;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import org.apache.wicket.markup.html.form.ChoiceRenderer;

public class SubsystemInCreateUserChoiceRenderer extends ChoiceRenderer<UISubsystemForList> {

    public Object getDisplayValue(UISubsystemForList object) {
        return (object!=null) ? object.getName(): "-- Please select --";
    }

    public String getIdValue(UISubsystemForList object, int arg1) {
        return object!=null ? String.valueOf(object.getId()): null;
    }

}
