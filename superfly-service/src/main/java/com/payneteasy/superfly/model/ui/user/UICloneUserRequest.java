package com.payneteasy.superfly.model.ui.user;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * Pseudo-entity which is used to pass a 'clone user' request to the jdbc-proc
 * driven DAO.
 * 
 * @author Roman Puchkovskiy
 */
public class UICloneUserRequest implements Serializable {
	private Long id;
	private String username;
	private String password;
	private Long templateUserId;

	@Column(name = "user_id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "new_user_name")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "new_user_password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "templete_user_id")
	public Long getTemplateUserId() {
		return templateUserId;
	}

	public void setTemplateUserId(Long templateUserId) {
		this.templateUserId = templateUserId;
	}
}
