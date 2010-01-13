package com.payneteasy.superfly.web.wicket.behavior;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;

/**
 * Behavior which, when added to a form field, causes it to sync its state
 * with model just when it was changed (using AJAX).
 * 
 * @author Roman Puchkovskiy
 */
public class OnlineFieldBehavior extends AjaxFormComponentUpdatingBehavior {

	public OnlineFieldBehavior() {
		super("onchange");
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target) {
	}

}
