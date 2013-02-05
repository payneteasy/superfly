package com.payneteasy.superfly.web.wicket.page.sso;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOLoginData implements Serializable {
    private String subsystemIdentifier;
    private String targetUrl;
    private String subsystemTitle;
    private String subsystemUrl;
    private String username;

    public SSOLoginData() {
    }

    public SSOLoginData(String subsystemIdentifier, String targetUrl) {
        this.subsystemIdentifier = subsystemIdentifier;
        this.targetUrl = targetUrl;
    }

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

    public String getSubsystemUrl() {
        return subsystemUrl;
    }

    public void setSubsystemUrl(String subsystemUrl) {
        this.subsystemUrl = subsystemUrl;
    }

    public String getSubsystemTitle() {
        return subsystemTitle;
    }

    public void setSubsystemTitle(String subsystemTitle) {
        this.subsystemTitle = subsystemTitle;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
