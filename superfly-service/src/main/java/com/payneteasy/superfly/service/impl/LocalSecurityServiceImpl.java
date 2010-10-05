package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;

@Transactional
public class LocalSecurityServiceImpl implements LocalSecurityService {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalSecurityServiceImpl.class);
	
	private UserDao userDao;
	private String localSubsystemName = "superfly";
	private String localRoleName = "admin";
	private LoggerSink loggerSink;

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setLocalSubsystemName(String localSubsystemName) {
		this.localSubsystemName = localSubsystemName;
	}

	public void setLocalRoleName(String localRoleName) {
		this.localRoleName = localRoleName;
	}

	@Required
	public void setLoggerSink(LoggerSink loggerSink) {
		this.loggerSink = loggerSink;
	}

	public String[] authenticate(String username, String password) {
		List<AuthRole> roles = userDao.authenticate(username, password,
				localSubsystemName, null, null);
		if (roles != null) {
			AuthRole role = null;
			for (AuthRole r : roles) {
				if (localRoleName.equals(r.getRoleName())) {
					role = r;
					break;
				}
			}
			if (role != null) {
				String[] result = new String[role.getActions().size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = role.getActions().get(i).getActionName();
				}
				loggerSink.info(logger, "LOCAL_LOGIN", true, username);
				return result;
			}
		}
		loggerSink.info(logger, "LOCAL_LOGIN", false, username);
		return null;
	}

}
