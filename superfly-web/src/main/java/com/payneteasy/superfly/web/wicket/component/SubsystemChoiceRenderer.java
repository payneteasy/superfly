package com.payneteasy.superfly.web.wicket.component;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import org.apache.wicket.markup.html.form.ChoiceRenderer;

@SuppressWarnings("serial")
public class SubsystemChoiceRenderer extends ChoiceRenderer<UISubsystemForFilter> {

    public Object getDisplayValue(UISubsystemForFilter object) {
        return (object!=null) ? object.getName(): "-- Please select --";
    }

    public String getIdValue(UISubsystemForFilter object, int index) {
        return object!=null ? String.valueOf(object.getId()): null;
    }

}
