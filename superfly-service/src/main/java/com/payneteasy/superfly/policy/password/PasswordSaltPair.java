package com.payneteasy.superfly.policy.password;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * Kuccyp
 * Date: 11.10.2010
 * Time: 8:07:23
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordSaltPair  implements Serializable{
    private String password;
    private String salt;

    @Column(name="user_password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name="salt")
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
