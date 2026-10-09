package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PCI DSS 2.1: logs an ERROR at startup while the built-in admin still has the documented default password.
 * The check goes through the same stored-hash comparison as the password history (legacy and pbkdf2 records).
 */
@Component
public class DefaultAdminPasswordStartupCheck implements SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminPasswordStartupCheck.class);

    static final String ADMIN_USER_NAME        = "admin";
    static final String DEFAULT_ADMIN_PASSWORD = "123admin123";

    private final UserService     userService;
    private final PasswordEncoder legacyPasswordEncoder;

    public DefaultAdminPasswordStartupCheck(UserService userService,
                                            @Qualifier("messageDigestPasswordEncoder") PasswordEncoder legacyPasswordEncoder) {
        this.userService           = userService;
        this.legacyPasswordEncoder = legacyPasswordEncoder;
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            if (hasDefaultPassword()) {
                logger.error("SECURITY: user '{}' still has the default password from the documentation; "
                        + "log in and change it immediately (PCI DSS 2.1)", ADMIN_USER_NAME);
            }
        } catch (RuntimeException e) {
            // the check is advisory: a DB hiccup must not stop the application from starting
            logger.warn("Could not check whether user '{}' has the default password: {}", ADMIN_USER_NAME, e.toString());
        }
    }

    boolean hasDefaultPassword() {
        List<PasswordSaltPair> stored = userService.getUserPasswordHistoryAndCurrentPassword(ADMIN_USER_NAME);
        if (stored == null || stored.isEmpty()) {
            return false;
        }
        // only the current password (the first record) matters, history entries are old passwords
        PasswordCheckContext context = new PasswordCheckContext(DEFAULT_ADMIN_PASSWORD, legacyPasswordEncoder, stored);
        return context.isPasswordExist(DEFAULT_ADMIN_PASSWORD, 0);
    }
}
