package com.payneteasy.superfly.model.ui.user;

import com.payneteasy.superfly.model.RoutineResult;

/**
 * @author rpuch
 */
public class UserCreationResult {
    private RoutineResult result;
    private String mailSendError;

    public RoutineResult getResult() {
        return result;
    }

    public void setResult(RoutineResult result) {
        this.result = result;
    }

    public String getMailSendError() {
        return mailSendError;
    }

    public void setMailSendError(String mailSendError) {
        this.mailSendError = mailSendError;
    }
}
