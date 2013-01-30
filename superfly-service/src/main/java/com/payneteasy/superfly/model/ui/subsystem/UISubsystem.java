package com.payneteasy.superfly.model.ui.subsystem;

import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

public class UISubsystem implements Serializable {
	private Long id;
	private String name;
	private String callbackInformation;
	private boolean allowListUsers;
    private UISmtpServerForFilter smtpServer;
    private String landingUrl;

	@Column(name = "ssys_id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	@Column(name = "allow_list_users")
	public boolean isAllowListUsers() {
		return allowListUsers;
	}

	public void setAllowListUsers(boolean allowListUsers) {
		this.allowListUsers = allowListUsers;
	}

    @ManyToOne
    @JoinColumn(table = "smtp_server", name = "ssrv_id")
    public UISmtpServerForFilter getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(UISmtpServerForFilter smtpServer) {
        this.smtpServer = smtpServer;
    }

    @Column(name = "landing_url")
    public String getLandingUrl() {
        return landingUrl;
    }

    public void setLandingUrl(String landingUrl) {
        this.landingUrl = landingUrl;
    }
}
