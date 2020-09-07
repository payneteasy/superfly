package com.payneteasy.superfly.password;

import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Kuccyp
 * Date: 08.10.2010
 * Time: 14:07:47
 * (C) 2010
 * Skype: kuccyp
 */
public class RandomStoredSaltSource implements SaltSource{

    private UserService userService;
    private SaltGenerator saltGenerator;

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setSaltGenerator(SaltGenerator saltGenerator) {
        this.saltGenerator = saltGenerator;
    }

    public String getSalt(String username) {
        String salt = userService.getUserSalt(username);
        if (salt == null || salt.isEmpty()) {
            salt = generateNewSaltAndSave(username);
        }
        return salt;
    }

    public String getSalt(long userId) {
        String salt = userService.getUserSaltByUserId(userId);
        if (salt == null || salt.isEmpty()) {
            salt = generateNewSaltAndSave(userId);
        }
        return salt;
    }

    private String generateNewSaltAndSave(String username) {
        String salt = generateSalt();
        userService.updateUserSalt(username, salt);
        return salt;
    }
    
    private String generateNewSaltAndSave(long userId) {
        String salt = generateSalt();
        userService.updateUserSaltByUserId(userId, salt);
        return salt;
    }

    private String generateSalt() {
        return saltGenerator.generate();
    }
}
