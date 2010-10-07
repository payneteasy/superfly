package com.payneteasy.superfly.web.wicket.validation;

import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 13:01:45
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordInputValidator extends AbstractFormValidator {

    private final FormComponent<?> theFormComponent;
    private final AbstractPolicyValidation<PasswordCheckContext> thePolicyValidation;

    public PasswordInputValidator(FormComponent<?> aFormComponent, AbstractPolicyValidation<PasswordCheckContext> aPolicyValidation) {
        theFormComponent = aFormComponent;
        thePolicyValidation=aPolicyValidation;
    }

    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent[] {theFormComponent};
    }

    public void validate(Form<?> form) {
        PasswordCheckContext password=new PasswordCheckContext(theFormComponent.getInput());
        try {
            thePolicyValidation.validate(password);
        } catch (PolicyValidationException e) {
            error(theFormComponent,e.getCode());
        }
    }
}
