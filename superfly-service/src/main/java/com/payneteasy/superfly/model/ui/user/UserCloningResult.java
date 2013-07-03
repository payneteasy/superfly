package com.payneteasy.superfly.model.ui.user;

import com.payneteasy.superfly.model.RoutineResult;

/**
 * @author rpuch
 */
public class UserCloningResult {
    private long cloneId;
    private String mailSendError;

    public long getCloneId() {
        return cloneId;
    }

    public void setCloneId(long cloneId) {
        this.cloneId = cloneId;
    }

    public String getMailSendError() {
        return mailSendError;
    }

    public void setMailSendError(String mailSendError) {
        this.mailSendError = mailSendError;
    }
}
