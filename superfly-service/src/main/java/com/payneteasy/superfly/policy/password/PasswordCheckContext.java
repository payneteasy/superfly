package com.payneteasy.superfly.policy.password;

import com.payneteasy.superfly.policy.IPolicyContext;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:14:15
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordCheckContext implements IPolicyContext{
    
    public PasswordCheckContext(String aPassword) {
        thePassword = aPassword;
    }

    /** Password */
      public String getPassword() { return thePassword; }

      /** Password */
      private final String thePassword;
}
