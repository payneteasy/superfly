package com.payneteasy.superfly.model;

import javax.persistence.Column;
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

    @Column(name = "subsystem_token")
    public String getSubsystemToken() {
        return subsystemToken;
    }

    public void setSubsystemToken(String subsystemToken) {
        this.subsystemToken = subsystemToken;
    }

    @Column(name = "landing_url")
    public String getLandingUrl() {
        return landingUrl;
    }

    public void setLandingUrl(String landingUrl) {
        this.landingUrl = landingUrl;
    }

    @Override
    public String toString() {
        return "SubsystemTokenData{" +
                "subsystemToken='" + subsystemToken + '\'' +
                ", landingUrl='" + landingUrl + '\'' +
                '}';
    }
}
