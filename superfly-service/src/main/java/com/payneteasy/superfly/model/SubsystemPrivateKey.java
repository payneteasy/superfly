package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * A stored subsystem private key as it is in the database (plain PEM until encrypted).
 */
public class SubsystemPrivateKey implements Serializable {
    private Long   id;
    private String name;
    private String privateKey;

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

    @Column(name = "private_key")
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return "SubsystemPrivateKey{id=" + id + ", name=" + name + '}';
    }
}
