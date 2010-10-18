package com.payneteasy.superfly.password;

import org.springframework.beans.factory.annotation.Required;

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
    private SaltGenerator saltGenerator;

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setSaltGenerator(SaltGenerator saltGenerator) {
		this.saltGenerator = saltGenerator;
	}

	public String getSalt(String username) {
        String salt=userDao.getUserSalt(username);
        if(salt==null || salt.isEmpty()){
            salt = generateNewSaltAndSave(username);
        }
        return salt;
    }
	
	public String getSalt(long userId) {
		String salt = userDao.getUserSaltByUserId(userId);
		if (salt == null || salt.isEmpty()) {
			salt = generateNewSaltAndSave(userId);
		}
		return salt;
	}

    private String generateNewSaltAndSave(String username) {
        String salt = generateSalt();
        userDao.updateUserSalt(username,salt);
        return salt;
    }
    
    private String generateNewSaltAndSave(long userId) {
        String salt = generateSalt();
        userDao.updateUserSaltByUserId(userId, salt);
        return salt;
    }

	private String generateSalt() {
		return saltGenerator.generate();
	}
}
