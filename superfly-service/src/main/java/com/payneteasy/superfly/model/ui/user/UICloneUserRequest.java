package com.payneteasy.superfly.model.ui.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * Pseudo-entity which is used to pass a 'clone user' request to the jdbc-proc
 * driven DAO.
 * 
 * @author Roman Puchkovskiy
 */
public class UICloneUserRequest implements Serializable {
	private static final long serialVersionUID = 1305784938422515978L;
	
	private Long id;
	private String username;
	private String password;
	private Long templateUserId;
	private String email;
    private String salt;
    private String hotpSalt;
    private String publicKey;
    private boolean isPasswordTemp;

	@Column(name = "user_id")
	@Id
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
	@Column(name = "new_user_email")
	public final String getEmail() {
		return email;
	}

	public final void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "template_user_id")
	public Long getTemplateUserId() {
		return templateUserId;
	}

	public void setTemplateUserId(Long templateUserId) {
		this.templateUserId = templateUserId;
	}

    @Column(name="new_user_salt")
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Column(name="new_hotp_salt")
	public String getHotpSalt() {
		return hotpSalt;
	}

	public void setHotpSalt(String hotpSalt) {
		this.hotpSalt = hotpSalt;
	}

	@Column(name = "public_key")
	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}


    @Column(name="is_password_temp")
    public boolean getIsPasswordTemp() {
        return isPasswordTemp;
    }

    public void setIsPasswordTemp(boolean isPasswordTemp) {
        this.isPasswordTemp = isPasswordTemp;
    }
}
