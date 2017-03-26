package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "temp_actions")
public class ActionToSave implements Serializable {
    private String name;
    private String description;

    public ActionToSave() {
    }

    public ActionToSave(String name) {
        this.name = name;
    }

    @Column(name = "action_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "action_description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
