package com.payneteasy.superfly.model.ui.action;

import javax.persistence.Column;

public class UIActionWithGroupForList extends UIActionForList {
    private String groupName;
    private long groupId;

    @Column(name = "group_name")
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Column(name = "grop_id")
    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
