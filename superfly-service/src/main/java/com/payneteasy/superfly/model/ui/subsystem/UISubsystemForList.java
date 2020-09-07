package com.payneteasy.superfly.model.ui.subsystem;

import java.io.Serializable;

import javax.persistence.Column;

public class UISubsystemForList implements Serializable {
    private long id;
    private String identifier;
    private String name;
    private String callbackInformation;
    private boolean sendCallbacks;
    private boolean fixed;
    private boolean allowListUsers;

    @Column(name = "ssys_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Column(name = "subsystem_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "callback_information")
    public String getCallbackInformation() {
        return callbackInformation;
    }

    public void setCallbackInformation(String callbackInformation) {
        this.callbackInformation = callbackInformation;
    }

    @Column(name = "send_callbacks")
    public boolean isSendCallbacks() {
        return sendCallbacks;
    }

    public void setSendCallbacks(boolean sendCallbacks) {
        this.sendCallbacks = sendCallbacks;
    }

    @Column(name = "fixed")
    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    @Column(name = "allow_list_users")
    public boolean isAllowListUsers() {
        return allowListUsers;
    }

    public void setAllowListUsers(boolean allowListUsers) {
        this.allowListUsers = allowListUsers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof UISubsystemForList))
            return false;
        UISubsystemForList other = (UISubsystemForList) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
