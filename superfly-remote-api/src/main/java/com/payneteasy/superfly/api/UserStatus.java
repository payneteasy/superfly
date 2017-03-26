package com.payneteasy.superfly.api;

import java.io.Serializable;
import java.util.Date;

/**
 * Describes a user's status.
 *
 * @author Roman Puchkovskiy
 * @since 1.3-5
 */
public class UserStatus implements Serializable {
    private String username;
    private boolean accountLocked;
    private Date lastLoginDate;
    private int loginsFailed;
    private Date lastFailedLoginDate;
    private String lastFailedLoginIp;

    /**
     * Returns a username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username    name to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns whether account is locked.
     *
     * @return true if account is locked
     */
    public boolean isAccountLocked() {
        return accountLocked;
    }

    /**
     * Sets whether account is locked.
     *
     * @param accountLocked true if locked
     */
    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    /**
     * Returns the last login date.
     *
     * @return last login date
     */
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Sets last login date.
     *
     * @param lastLoginDate last login date
     */
    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /**
     * Returns number of failed login attempts.
     *
     * @return logins failed
     */
    public int getLoginsFailed() {
        return loginsFailed;
    }

    /**
     * Sets number of failed login attempts.
     *
     * @param loginsFailed number to set
     */
    public void setLoginsFailed(int loginsFailed) {
        this.loginsFailed = loginsFailed;
    }

    /**
     * Returns last failed login date.
     *
     * @return date of the last failed login
     */
    public Date getLastFailedLoginDate() {
        return lastFailedLoginDate;
    }

    /**
     * Sets last failed login date.
     *
     * @param lastFailedLoginDate   date to set
     */
    public void setLastFailedLoginDate(Date lastFailedLoginDate) {
        this.lastFailedLoginDate = lastFailedLoginDate;
    }

    /**
     * Sets IP from which last failed login attempt was made.
     *
     * @return IP
     */
    public String getLastFailedLoginIp() {
        return lastFailedLoginIp;
    }

    /**
     * Sets IP from which last failed login attempt was made.
     *
     * @param lastFailedLoginIp IP to set
     */
    public void setLastFailedLoginIp(String lastFailedLoginIp) {
        this.lastFailedLoginIp = lastFailedLoginIp;
    }
}
