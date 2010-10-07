package com.payneteasy.superfly.policy.password;

import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.IPolicyContext;

import java.util.Collections;
import java.util.List;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:14:15
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordCheckContext implements IPolicyContext{
    public PasswordCheckContext(String aPassword) {
        this(aPassword,null,null,null, Collections.<String>emptyList());
    }

    public PasswordCheckContext(String aPassword,String aUsername,PasswordEncoder aPasswordEncoder,SaltSource aSaltSource, List<String> aPasswordHistory) {
        thePassword = aPassword;
        theUsername = aUsername;
        thePasswordEncoder = aPasswordEncoder;
        theSaltSource = aSaltSource;
        thePasswordHistory=aPasswordHistory;
    }

    /** Password */
      public String getPassword() { return thePassword; }

      public boolean isPasswordExit(String aPassword,int aHistoryLength){

          int length=0;
          for(String pwd:thePasswordHistory){
              if(length<aHistoryLength && pwd.equals(thePasswordEncoder.encode(aPassword,theSaltSource.getSalt(theUsername)))){
                  return true;
              }
              length++;
          }
          return false;
      }

      /** Password */
      private final String thePassword;
      private final String theUsername;
      private final PasswordEncoder thePasswordEncoder;
      private final SaltSource theSaltSource;
      private final List<String> thePasswordHistory;
}
