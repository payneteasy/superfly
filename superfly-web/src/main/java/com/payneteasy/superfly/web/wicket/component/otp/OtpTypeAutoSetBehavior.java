package com.payneteasy.superfly.web.wicket.component.otp;

import com.payneteasy.superfly.model.ui.user.OtpTypeDefaults;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;

/**
 * Keeps the invariant "OTP mandatory =&gt; OTP type is not none" visible in the form:
 * as soon as the edited user would end up with mandatory OTP and no OTP type, the type
 * is set to the default one and the administrator is told about it.
 * <p>
 * Attach it to both the "is OTP optional" check box and the OTP type drop down: an ajax
 * request submits only the component it originates from, so a behavior on the check box
 * alone would decide on a stale OTP type (and vice versa).
 *
 * @see com.payneteasy.superfly.service.UserService#updateUser the same rule is enforced server-side
 */
public class OtpTypeAutoSetBehavior extends OnChangeAjaxBehavior {

    private static final String MESSAGE_KEY = "user.otpTypeAutoSet";

    private final UIUser                        user;
    private final LabelDropDownChoiceRow<String> otpTypeRow;
    private final Component                     feedbackPanel;

    public OtpTypeAutoSetBehavior(UIUser user, LabelDropDownChoiceRow<String> otpTypeRow, Component feedbackPanel) {
        this.user = user;
        this.otpTypeRow = otpTypeRow;
        this.feedbackPanel = feedbackPanel;
    }

    @Override
    protected void onUpdate(AjaxRequestTarget target) {
        if (!OtpTypeDefaults.needsDefaultType(user)) {
            return;
        }

        user.setOtpType(OtpTypeDefaults.MANDATORY_DEFAULT.code());
        // a form component renders the value it received, not the model, so a stale
        // selection (a previous failed submit, or the choice we have just overridden)
        // would survive the re-render
        otpTypeRow.clearInput();

        getComponent().info(getComponent().getString(MESSAGE_KEY));
        target.add(otpTypeRow, feedbackPanel);
    }
}
