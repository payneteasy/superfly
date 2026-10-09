package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * What subsystem authentication needs to know about a subsystem; unlike {@code UISubsystem} it carries the token.
 */
public class SubsystemAuth implements Serializable {
    private Long   id;
    private String name;
    private String subsystemToken;
    private String encryptionAlgorithm;

    @Column(name = "ssys_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "subsystem_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "subsystem_token")
    public String getSubsystemToken() {
        return subsystemToken;
    }

    public void setSubsystemToken(String subsystemToken) {
        this.subsystemToken = subsystemToken;
    }

    @Column(name = "encryption_algorithm")
    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
}
