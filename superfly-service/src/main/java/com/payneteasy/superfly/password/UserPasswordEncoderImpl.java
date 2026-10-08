package com.payneteasy.superfly.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UserPasswordEncoderImpl implements UserPasswordEncoder {
    private PasswordEncoder passwordEncoder;
    private PasswordEncoder legacyPasswordEncoder;
    private SaltSource saltSource;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    @Qualifier("messageDigestPasswordEncoder")
    public void setLegacyPasswordEncoder(PasswordEncoder legacyPasswordEncoder) {
        this.legacyPasswordEncoder = legacyPasswordEncoder;
    }

    @Autowired
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    public String encode(String plaintextPassword, String username) {
        return passwordEncoder.encode(plaintextPassword, saltSource.getSalt(username));
    }

    public String encodeLegacy(String plaintextPassword, String username) {
        return legacyPasswordEncoder.encode(plaintextPassword, saltSource.getSalt(username));
    }

    public String encode(String plaintextPassword, long userId) {
        return passwordEncoder.encode(plaintextPassword, saltSource.getSalt(userId));
    }
}
