package com.payneteasy.superfly.web.wicket.page.login;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOLoginData implements Serializable {
    private String subsystemIdentifier;
    private String targetUrl;

    public String getSubsystemIdentifier() {
        return subsystemIdentifier;
    }

    public void setSubsystemIdentifier(String subsystemIdentifier) {
        this.subsystemIdentifier = subsystemIdentifier;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
