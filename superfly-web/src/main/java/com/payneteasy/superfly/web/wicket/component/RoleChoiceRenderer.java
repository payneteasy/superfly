package com.payneteasy.superfly.web.wicket.component;

import org.apache.wicket.markup.html.form.IChoiceRenderer;

import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;

/**
 * Renders a choice for a role.
 * 
 * @author Roman Puchkovskiy
 */
public class RoleChoiceRenderer implements IChoiceRenderer<UIRoleForFilter> {

    public Object getDisplayValue(UIRoleForFilter object) {
        if (object == null) {
            return null;
        }
        return object.getSubsystemName() + " - " + object.getRoleName();
    }

    public String getIdValue(UIRoleForFilter object, int index) {
        if (object == null) {
            return null;
        }
        return String.valueOf(object.getId());
    }

}
