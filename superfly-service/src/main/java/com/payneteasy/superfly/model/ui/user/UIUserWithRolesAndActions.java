package com.payneteasy.superfly.model.ui.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;

public class UIUserWithRolesAndActions implements Serializable {
	private long id;
	private String name;
	private String publicKey;
	
	private List<UIRoleWithActions> roles = new ArrayList<UIRoleWithActions>();

	@Column(name = "user_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "user_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "public_key")
	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@OneToMany
	@JoinColumn(table = "role")
	public List<UIRoleWithActions> getRoles() {
		return roles;
	}

	public void setRoles(List<UIRoleWithActions> roles) {
		this.roles = roles;
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
		UIUserWithRolesAndActions other = (UIUserWithRolesAndActions) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
