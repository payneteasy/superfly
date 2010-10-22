package com.payneteasy.superfly.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.service.SyslogService;
import com.payneteasy.superfly.service.UserInfoService;

public class SyslogServiceImpl implements SyslogService {
	private Logger apacheLogger = Logger.getLogger(SyslogServiceImpl.class);
	private UserInfoService userInfoService;

	@Required
	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public void sendLogMessage(String eventType, boolean success, String resourceIdentity) {
		String username = userInfoService.getUsername();
		String usernameFormatted;
		if (username == null) {
			usernameFormatted = "<null>";
		} else {
			usernameFormatted = username;
		}
		String message = String.format("%s:%s:%s:%s", usernameFormatted, eventType, resourceIdentity,
				success ? "success" : "failure");
		apacheLogger.info(message);
	}

}
