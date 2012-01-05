package com.payneteasy.superfly.model.ui.subsystem;

import java.io.Serializable;

import javax.persistence.Column;

public class UISubsystemForFilter implements Serializable {
	private long id;
	private String name;

	@Column(name = "ssys_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "subsystem_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UISubsystemForFilter other = (UISubsystemForFilter) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
