package com.payneteasy.superfly.password;

import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.pcidss.PCIDSSPasswordPolicyValidation;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class PasswordPolicyTest {

    @Test
    public void testComplexity() {
        AbstractPolicyValidation<PasswordCheckContext> validation=new PCIDSSPasswordPolicyValidation();

        PasswordCheckContext password=new PasswordCheckContext(null);

        // validate empty password
        try{
            validation.validate(password);
        } catch (PolicyValidationException e){
            assertEquals(e.getCode(), PolicyValidationException.EMPTY_PASSWORD);
        }

        password=new PasswordCheckContext("12345");

        // validate short password
        try{
            validation.validate(password);
        } catch (PolicyValidationException e){
            assertEquals(e.getCode(),PolicyValidationException.SHORT_PASSWORD);
        }

        password=new PasswordCheckContext("1234567");

        // validate simple password
        try{
            validation.validate(password);
        } catch (PolicyValidationException e){
            assertEquals(e.getCode(), PolicyValidationException.SIMPLE_PASSWORD);
        }


        boolean throwsException=false;

        // validate normal password
        password=new PasswordCheckContext("#asdfBsdf74");
        try{
            validation.validate(password);
        } catch (PolicyValidationException e){
            System.out.println(e.getMessage());
            throwsException=true;
        }

        Assert.assertFalse(throwsException);
    }

}
