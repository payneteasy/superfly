package com.payneteasy.superfly.policy.password;

import java.util.Collections;
import java.util.List;

import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
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
              if(length<aHistoryLength + 1 && matchesStored(aPassword, pwd)){
                  return true;
              }
              length++;
          }
          return false;
      }

      // thePasswordEncoder is the legacy one: it is used only for records which are not in pbkdf2 format
      private boolean matchesStored(String aPassword, PasswordSaltPair pwd) {
          if (Pbkdf2PasswordEncoder.isPbkdf2(pwd.getPassword())) {
              return Pbkdf2PasswordEncoder.matches(aPassword, pwd.getSalt(), pwd.getPassword());
          }
          return thePasswordEncoder.encode(aPassword, pwd.getSalt()).equals(pwd.getPassword());
      }

      /** Password */
      private final String thePassword;
      private final PasswordEncoder thePasswordEncoder;
      private final List<PasswordSaltPair> thePasswordHistory;
}
