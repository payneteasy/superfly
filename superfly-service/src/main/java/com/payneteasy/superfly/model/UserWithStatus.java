package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.util.Date;

/**
 * @author rpuch
 */
public class UserWithStatus extends User {
    private boolean accountLocked;
    private Date lastLoginDate;
    private int loginsFailed;
    private Date lastFailedLoginDate;
    private String lastFailedLoginIp;

    @Column(name = "is_account_locked")
    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    @Column(name = "last_login_date")
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @Column(name = "logins_failed")
    public int getLoginsFailed() {
        return loginsFailed;
    }

    public void setLoginsFailed(int loginsFailed) {
        this.loginsFailed = loginsFailed;
    }

    @Column(name = "last_failed_login_date")
    public Date getLastFailedLoginDate() {
        return lastFailedLoginDate;
    }

    public void setLastFailedLoginDate(Date lastFailedLoginDate) {
        this.lastFailedLoginDate = lastFailedLoginDate;
    }

    @Column(name = "last_failed_login_ip")
    public String getLastFailedLoginIp() {
        return lastFailedLoginIp;
    }

    public void setLastFailedLoginIp(String lastFailedLoginIp) {
        this.lastFailedLoginIp = lastFailedLoginIp;
    }
}
