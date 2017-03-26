package com.payneteasy.superfly.password;

/**
 * Password encoder base.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractPasswordEncoder implements PasswordEncoder {

    protected String mergePasswordAndSalt(String plainPassword, String salt) {
        if (salt == null || salt.length() == 0) {
            return plainPassword;
        } else {
            return plainPassword + "{" + salt + "}";
        }
    }

}