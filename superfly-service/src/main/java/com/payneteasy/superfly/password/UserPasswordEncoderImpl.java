package com.payneteasy.superfly.password;

import org.springframework.beans.factory.annotation.Required;

public class UserPasswordEncoderImpl implements UserPasswordEncoder {
    private PasswordEncoder passwordEncoder;
    private SaltSource saltSource;

    @Required
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Required
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    public String encode(String plaintextPassword, String username) {
        return passwordEncoder.encode(plaintextPassword, saltSource.getSalt(username));
    }

    public String encode(String plaintextPassword, long userId) {
        return passwordEncoder.encode(plaintextPassword, saltSource.getSalt(userId));
    }
}
