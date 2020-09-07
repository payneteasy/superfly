package com.payneteasy.superfly.web.wicket.component;

import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import org.apache.wicket.markup.html.form.ChoiceRenderer;

public class RoleInCreateUserChoiceRender extends ChoiceRenderer<UIRoleForList> {

    public Object getDisplayValue(UIRoleForList object) {
        return (object!=null) ? object.getName(): "-- Please select --";
    }

    public String getIdValue(UIRoleForList object, int arg1) {
        return object!=null ? String.valueOf(object.getId()): null;
    }

}
