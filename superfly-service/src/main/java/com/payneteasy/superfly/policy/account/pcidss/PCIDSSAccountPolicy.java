package com.payneteasy.superfly.policy.account.pcidss;

import java.util.List;

import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.User;
import com.payneteasy.superfly.password.PasswordGenerator;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.UserService;
import org.springframework.stereotype.Component;

/**
 * {@link AccountPolicy} which conforms to PCI-DSS requirements.
 *
 * @author Roman Puchkovskiy
 */
@Component
@OnPolicyCondition(Policy.PCIDSS)
public class PCIDSSAccountPolicy implements AccountPolicy {

    private static final Logger logger = LoggerFactory.getLogger(PCIDSSAccountPolicy.class);

    private UserService userService;
    private PasswordGenerator passwordGenerator;
    private UserPasswordEncoder userPasswordEncoder;
    private ResetPasswordStrategy resetPasswordStrategy;

    @Autowired
    public void setResetPasswordStrategy(ResetPasswordStrategy resetPasswordStrategy) {
        this.resetPasswordStrategy = resetPasswordStrategy;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setUserPasswordEncoder(UserPasswordEncoder userPasswordEncoder) {
        this.userPasswordEncoder = userPasswordEncoder;
    }

    public String unlockUser(long userId, boolean unlockingSuspendedUser) {
        if (unlockingSuspendedUser) {
            String password = passwordGenerator.generate();
            String encPassword = userPasswordEncoder.encode(password, userId);
            RoutineResult result = userService.unlockSuspendedUser(userId, encPassword);
            if (!result.isOk()) {
                throw new IllegalStateException(result.getErrorMessage());
            }
            return password;
        } else {
            RoutineResult result = userService.unlockUser(userId);
            if (!result.isOk()) {
                throw new IllegalStateException(result.getErrorMessage());
            }
            return null;
        }
    }

    public void suspendUsersIfNeeded(int days, UserService userService) {
        List<User> users = userService.getUsersToSuspend(days);
        for (User user : users) {
            logger.debug(String.format("Suspending user [%s] with id=%d", user.getUserName(), user.getUserid()));
            userService.suspendUser(user.getUserid());
        }
    }

    public void expirePasswordsIfNeeded(int days, UserService userService) {
        List<User> users = userService.getUsersWithExpiredPasswords(days);
        for (User u : users) {
            logger.debug(String.format("Reset password for user [%s] with id=%d", u.getUserName(), u.getUserid()));
            resetPasswordStrategy.resetPassword(u.getUserid(), u.getUserName(), null);
        }
    }

}
