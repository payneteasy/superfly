package com.payneteasy.superfly.service.impl;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.service.SyslogService;
import com.payneteasy.superfly.service.UserInfoService;

public class SyslogServiceImpl implements SyslogService {
	private UserInfoService userInfoService;
	private String syslogIp;

	@Required
	public void setSyslogIp(String syslogIp) {
		this.syslogIp = syslogIp;
	}

	@Required
	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public void sendLogMessage(Logger logger, String eventType, boolean success, String resourceIdentity) {
		SyslogAppender sa = new SyslogAppender();
		sa.setSyslogHost(syslogIp);
		sa.setFacility("USER");
		sa.setLayout(new PatternLayout());
		sa.activateOptions();
		logger.addAppender(sa);

		String username = userInfoService.getUsername();
		String usernameFormatted;
		if (username == null) {
			usernameFormatted = "<null>";
		} else {
			usernameFormatted = username;
		}
		String message = String.format("%s:%s:%s:%s", usernameFormatted, eventType, resourceIdentity,
				success ? "success" : "failure");
		logger.info(message);
	}

}
