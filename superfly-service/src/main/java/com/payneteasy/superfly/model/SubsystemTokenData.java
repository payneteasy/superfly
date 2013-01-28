package com.payneteasy.superfly.model;

import java.io.Serializable;

/**
* @author rpuch
*/
public class SubsystemTokenData implements Serializable {
    private String subsystemToken;
    private String landingUrl;

    public SubsystemTokenData() {
    }

    public SubsystemTokenData(String subsystemToken, String landingUrl) {
        this.subsystemToken = subsystemToken;
        this.landingUrl = landingUrl;
    }

    public String getSubsystemToken() {
        return subsystemToken;
    }

    public void setSubsystemToken(String subsystemToken) {
        this.subsystemToken = subsystemToken;
    }

    public String getLandingUrl() {
        return landingUrl;
    }

    public void setLandingUrl(String landingUrl) {
        this.landingUrl = landingUrl;
    }
}
