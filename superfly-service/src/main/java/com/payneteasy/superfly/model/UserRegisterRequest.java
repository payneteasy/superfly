package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;

public class UserRegisterRequest implements Serializable {
	private static final long serialVersionUID = -4093140271801747768L;

	private long userid;
	private String username;
	private String password;
	private String email;
	private String subsystemName;
	private String principalNames;
	private String name;
	private String surname;
	private String secretQuestion;
	private String secretAnswer;
	private String salt;
	private String isPasswordTemp;
    private String hotpSalt;
    private String publicKey;

	@Column(name = "user_id")
	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	@Column(name = "user_name")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "user_password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "user_email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "subsystem_name")
	public String getSubsystemName() {
		return subsystemName;
	}

	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}

	@Column(name = "principal_list")
	public String getPrincipalNames() {
		return principalNames;
	}

	public void setPrincipalNames(String principalNames) {
		this.principalNames = principalNames;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "surname")
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	@Column(name = "secret_question")
	public String getSecretQuestion() {
		return secretQuestion;
	}

	public void setSecretQuestion(String secretQuestion) {
		this.secretQuestion = secretQuestion;
	}

	@Column(name = "secret_answer")
	public String getSecretAnswer() {
		return secretAnswer;
	}

	public void setSecretAnswer(String secretAnswer) {
		this.secretAnswer = secretAnswer;
	}

	@Column(name = "salt")
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
	@Column(name="is_password_temp")
	public String getIsPasswordTemp() {
		return isPasswordTemp;
	}

	public void setIsPasswordTemp(String isPasswordTemp) {
		this.isPasswordTemp = isPasswordTemp;
	}

    @Column(name="hotp_salt")
	public String getHotpSalt() {
		return hotpSalt;
	}

	public void setHotpSalt(String hotpSalt) {
		this.hotpSalt = hotpSalt;
	}

	@Column(name="public_key")
	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
