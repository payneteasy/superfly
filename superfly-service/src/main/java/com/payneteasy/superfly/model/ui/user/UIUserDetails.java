package com.payneteasy.superfly.model.ui.user;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * Date: 09.04.13 Time: 11:38
 */
public class UIUserDetails extends UIUser implements Serializable{
    private boolean accountLocked;
    private boolean accountSuspended;

    @Column(name = "is_account_locked")
    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    @Column(name = "is_account_suspended")
    public boolean isAccountSuspended() {
        return accountSuspended;
    }

    public void setAccountSuspended(boolean accountSuspended) {
        this.accountSuspended = accountSuspended;
    }

}
