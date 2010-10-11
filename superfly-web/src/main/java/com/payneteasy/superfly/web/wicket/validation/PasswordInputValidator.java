package com.payneteasy.superfly.web.wicket.validation;

import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.service.UserService;
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

    private final FormComponent<?> theFormComponentUserName;
    private final FormComponent<?> theFormComponentPassword;
    private final String theUserName;
    private final UserService theUserService;

    public PasswordInputValidator(FormComponent<?> aFormComponentUserName,FormComponent<?> aFormComponentPassword, UserService aUserService) {
        theFormComponentUserName = aFormComponentUserName;
        theFormComponentPassword=aFormComponentPassword;
        theUserService= aUserService;
        theUserName=null;
    }

    public PasswordInputValidator(String aUserName,FormComponent<?> aFormComponentPassword, UserService aUserService) {
        theFormComponentUserName = null;
        theFormComponentPassword=aFormComponentPassword;
        theUserService= aUserService;
        theUserName=aUserName;
    }
    
    public FormComponent<?>[] getDependentFormComponents() {
        if(theUserName==null){
            return new FormComponent[]{theFormComponentUserName,theFormComponentPassword};
        } else {
            return new FormComponent[]{theFormComponentPassword};
        }
    }

    public void validate(Form<?> form) {
        try {
            theUserService.validatePassword(getUserName(),theFormComponentPassword.getInput());
        } catch (PolicyValidationException e) {
            error(theFormComponentPassword, e.getCode());
        }
    }

    private String getUserName() {
        if(theUserName==null) {
            return theFormComponentUserName.getInput();
        } else {
            return theUserName;
        }
    }
}
