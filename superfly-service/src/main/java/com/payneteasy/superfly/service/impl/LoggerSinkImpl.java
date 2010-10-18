package com.payneteasy.superfly.service.impl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserInfoService;

public class LoggerSinkImpl implements LoggerSink {
	
	private UserInfoService userInfoService;
	
	@Required
	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public void info(Logger logger, String eventType, boolean success, String resourceIdentity) {
		String username = userInfoService.getUsername();
		String usernameFormatted;
		if (username == null) {
			usernameFormatted = "<null>";
		} else {
			usernameFormatted = username;
		}
		String message = String.format("%s:%s:%s:%s", usernameFormatted,
				eventType, resourceIdentity, success ? "success" : "failure");
		logger.info(message);
	}

}
