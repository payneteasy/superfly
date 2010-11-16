package com.payneteasy.superfly.policy.password;

import java.util.Collections;
import java.util.List;

import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.policy.IPolicyContext;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:14:15
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordCheckContext implements IPolicyContext {
    public PasswordCheckContext(String aPassword) {
        this(aPassword,null,Collections.<PasswordSaltPair>emptyList());
    }

    public PasswordCheckContext(String aPassword, PasswordEncoder aPasswordEncoder, List<PasswordSaltPair> aPasswordHistory) {
        thePassword = aPassword;
        thePasswordEncoder = aPasswordEncoder;
        thePasswordHistory = aPasswordHistory;
    }

    /** Password */
      public String getPassword() { return thePassword; }

      public boolean isPasswordExist(String aPassword,int aHistoryLength){

          int length=0;
          for(PasswordSaltPair pwd:thePasswordHistory){
        	  // +1 in the following line is because the current password is
        	  // first in the list (as it must not be present in the history)
              if(length<aHistoryLength + 1 && thePasswordEncoder.encode(aPassword,pwd.getSalt()).equals(pwd.getPassword())){
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
