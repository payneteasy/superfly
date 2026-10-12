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
    // one-time value from the subsystem, returned to it together with the subsystem token
    private String state;

    private String otpTypeCode;
    private boolean isOtpOptional;
    // set only after the password step for a user who must enrol Google Auth for the first time
    private boolean googleAuthSetupRequired;
    // set by the password step when it accepts a temporary password: the login ends with a password change
    private boolean passwordChangeRequired;
    // set when such a login has passed the second factor (or needs none): the password may be changed now
    private boolean passwordChangeAllowed;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOtpTypeCode() {
        return otpTypeCode;
    }

    public void setOtpTypeCode(String otpTypeCode) {
        this.otpTypeCode = otpTypeCode;
    }

    public boolean isOtpOptional() {
        return isOtpOptional;
    }

    public void setOtpOptional(boolean otpOptional) {
        isOtpOptional = otpOptional;
    }

    public boolean isGoogleAuthSetupRequired() {
        return googleAuthSetupRequired;
    }

    public void setGoogleAuthSetupRequired(boolean googleAuthSetupRequired) {
        this.googleAuthSetupRequired = googleAuthSetupRequired;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public void setPasswordChangeRequired(boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public boolean isPasswordChangeAllowed() {
        return passwordChangeAllowed;
    }

    public void setPasswordChangeAllowed(boolean passwordChangeAllowed) {
        this.passwordChangeAllowed = passwordChangeAllowed;
    }

    @Override
    public String toString() {
        return "SSOLoginData{" +
                "subsystemIdentifier='" + subsystemIdentifier + '\'' +
                ", targetUrl='" + targetUrl + '\'' +
                ", subsystemTitle='" + subsystemTitle + '\'' +
                ", subsystemUrl='" + subsystemUrl + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
