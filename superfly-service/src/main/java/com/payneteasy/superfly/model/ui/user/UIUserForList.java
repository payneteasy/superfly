package com.payneteasy.superfly.model.ui.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;

/**
 * User object to be used in the UI (user list).
 * 
 * @author Roman Puchkovskiy
 */
public class UIUserForList implements Serializable {
    private long id;
    private String username;
    private String password;
    private boolean accountLocked;
    private int loginsFailed;
    private Date lastLoginDate;
    private String email;
    private boolean accountSuspended;
    private long nextOtpCounter;

    @Column(name = "user_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "user_name")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "user_password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "is_account_locked")
    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    @Column(name = "logins_failed")
    public int getLoginsFailed() {
        return loginsFailed;
    }

    public void setLoginsFailed(int loginsFailed) {
        this.loginsFailed = loginsFailed;
    }

    @Column(name = "last_login_date")
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @Column(name = "email")
    public final String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "is_account_suspended")
    public boolean isAccountSuspended() {
        return accountSuspended;
    }

    public void setAccountSuspended(boolean accountSuspended) {
        this.accountSuspended = accountSuspended;
    }

    @Column(name = "hotp_counter")
    public long getNextOtpCounter() {
        return nextOtpCounter;
    }

    public void setNextOtpCounter(long nextOtpCounter) {
        this.nextOtpCounter = nextOtpCounter;
    }

}
