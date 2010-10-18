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
        this(aPassword,null,Collections.<PasswordSaltPair>emptyList());
    }

    public PasswordCheckContext(String aPassword,PasswordEncoder aPasswordEncoder,List<PasswordSaltPair> aPasswordHistory) {
        thePassword = aPassword;
        thePasswordEncoder = aPasswordEncoder;
        thePasswordHistory=aPasswordHistory;
    }

    /** Password */
      public String getPassword() { return thePassword; }

      public boolean isPasswordExit(String aPassword,int aHistoryLength){

          int length=0;
          for(PasswordSaltPair pwd:thePasswordHistory){
              if(length<aHistoryLength && thePasswordEncoder.encode(aPassword,pwd.getSalt()).equals(pwd.getPassword())){
                  return true;
              }
              length++;
          }
          return false;
      }

      /** Password */
      private final String thePassword;
      private final PasswordEncoder thePasswordEncoder;
      private final List<PasswordSaltPair> thePasswordHistory;
}
