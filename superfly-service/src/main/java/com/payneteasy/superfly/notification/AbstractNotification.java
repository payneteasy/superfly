package com.payneteasy.superfly.notification;

import java.io.Serializable;

/**
 * Base for all subsystem notifications.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractNotification implements Serializable {
    private static final long serialVersionUID = 3016688765672360571L;

    private String callbackUri;

    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }
}
