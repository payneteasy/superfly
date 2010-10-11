package com.payneteasy.superfly.password;

import com.payneteasy.superfly.common.utils.CryptoHelper;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.utils.RandomGUID;

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
        String salt=userDao.getUserSalt(username);
        if(salt==null || salt.isEmpty()){
            return generateNewSalt(username);
        }
        return salt;
    }

    private String generateNewSalt(String username) {
        RandomGUID guid=new RandomGUID(true);
        String salt=CryptoHelper.SHA256(guid.toString());
        userDao.updateUserSalt(username,salt);
        return salt;
    }
}
