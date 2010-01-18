package com.payneteasy.superfly.model.ui.group;

import java.io.Serializable;

import javax.persistence.Column;

public class UICloneGroupRequest implements Serializable {
//	i_new_group_name varchar(32),
//    i_templete_grop_id int(10),
	private long id;
	private String newGroupName; 
	private long sourceGroupId;
	
	@Column(name = "grop_id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Column(name = "new_group_name")
	public String getNewGroupName() {
		return newGroupName;
	}
	public void setNewGroupName(String newGroupName) {
		this.newGroupName = newGroupName;
	}
	@Column(name = "templete_grop_id")
	public long getSourceGroupId() {
		return sourceGroupId;
	}
	public void setSourceGroupId(long sourceGroupId) {
		this.sourceGroupId = sourceGroupId;
	}
	
	
}
