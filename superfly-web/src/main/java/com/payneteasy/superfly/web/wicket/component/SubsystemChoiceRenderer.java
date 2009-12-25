package com.payneteasy.superfly.web.wicket.component;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import com.payneteasy.superfly.model.ui.*;
@SuppressWarnings("serial")
public class SubsystemChoiceRenderer implements IChoiceRenderer<UISubsystemForFilter>{

	public Object getDisplayValue(UISubsystemForFilter object) {
		return (object!=null) ? object.getName(): null;
	}

	public String getIdValue(UISubsystemForFilter object, int index) {
		return object!=null ? String.valueOf(object.getId()): null;
	}

}
