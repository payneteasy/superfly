package com.payneteasy.superfly.model.ui.group;

import java.io.Serializable;

import javax.persistence.Column;

public class UIGroupForList implements Serializable {
	private long id;
	private String name;
	
	@Column(name = "grop_id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Column(name = "group_name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
