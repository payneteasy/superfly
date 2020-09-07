package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * Kuccyp
 * Date: 12.10.2010
 * Time: 13:06:48
 * (C) 2010
 * Skype: kuccyp
 */
public class User implements Serializable{
    
    private long userid;
    private String userName;

    @Column(name="user_id")
    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }
    

    @Column(name="user_name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
