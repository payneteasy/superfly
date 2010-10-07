package com.payneteasy.superfly.password;

import com.payneteasy.superfly.policy.IPolicyContext;
import com.payneteasy.superfly.policy.PolicyException;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.pcidss.PCIDSSPasswordPolicyValidation;
import junit.framework.TestCase;


public class PasswordPolicyTest extends TestCase {



	public void testComplexity() {



        AbstractPolicyValidation validation=new PCIDSSPasswordPolicyValidation();

        IPolicyContext password=new PasswordCheckContext(null);

        // validate empty password
        try{
            validation.validate(password);
        } catch (PolicyException e){
            assertEquals(e.getCode(),PolicyException.EMPTY_PASSWORD);
        }

        password=new PasswordCheckContext("12345");

        // validate short password
        try{
            validation.validate(password);
        } catch (PolicyException e){
            assertEquals(e.getCode(),PolicyException.SHORT_PASSWORD);
        }
        
        password=new PasswordCheckContext("1234567");

        // validate simple password
        try{
            validation.validate(password);
        } catch (PolicyException e){
            assertEquals(e.getCode(),PolicyException.SIMPLE_PASSWORD);
        }


        boolean throwsException=false;

        // validate normal password
        password=new PasswordCheckContext("ивnmdn74");
        try{
            validation.validate(password);
        } catch (PolicyException e){
            throwsException=true;
        }

        assertFalse(throwsException);
	}

}