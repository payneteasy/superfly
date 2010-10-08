package com.payneteasy.superfly.password;

import com.payneteasy.superfly.dao.UserDao;

/**
 * Kuccyp
 * Date: 08.10.2010
 * Time: 14:07:47
 * (C) 2010
 * Skype: kuccyp
 */
public class RandomStoredSaltSource implements SaltSource{

    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String getSalt(String username) {
        return username;
    }
}
