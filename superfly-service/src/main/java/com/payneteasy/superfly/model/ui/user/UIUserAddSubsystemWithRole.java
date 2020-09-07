package com.payneteasy.superfly.model.ui.user;

import java.io.Serializable;

import javax.persistence.Column;

public class UIUserAddSubsystemWithRole implements Serializable {
    private long userId;
    private long roleId;
    @Column(name = "user_id")
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
    @Column(name = "role_id")
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

}
