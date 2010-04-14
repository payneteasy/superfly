package com.payneteasy.superfly.web.wicket.component;

import org.apache.wicket.markup.html.form.IChoiceRenderer;

import com.payneteasy.superfly.model.ui.role.UIRoleForList;

public class RoleInCreateUserChoiceRender implements IChoiceRenderer<UIRoleForList>{

	public Object getDisplayValue(UIRoleForList object) {
		return (object!=null) ? object.getName(): "-- Please select --";
	}

	public String getIdValue(UIRoleForList object, int arg1) {
		return object!=null ? String.valueOf(object.getId()): null;
	}

}
