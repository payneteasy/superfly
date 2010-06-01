package com.payneteasy.superfly.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.dao.ActionDao;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthAction;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.RegisterUser;
import com.payneteasy.superfly.model.UserWithActions;
import com.payneteasy.superfly.service.InternalSSOService;

@Transactional
public class InternalSSOServiceImpl implements InternalSSOService {

	private static final Logger logger = LoggerFactory
			.getLogger(InternalSSOServiceImpl.class);

	private UserDao userDao;
	private ActionDao actionDao;

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Required
	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
	}

	public SSOUser authenticate(String username, String password,
			String subsystemIdentifier, String userIpAddress, String sessionInfo) {
		SSOUser ssoUser;
		List<AuthRole> authRoles = userDao.authenticate(username, password,
				subsystemIdentifier, userIpAddress, sessionInfo);
		if (authRoles != null && !authRoles.isEmpty()) {
			Map<SSORole, SSOAction[]> actionsMap = new HashMap<SSORole, SSOAction[]>(
					authRoles.size());
			for (AuthRole authRole : authRoles) {
				SSORole ssoRole = new SSORole(authRole.getRoleName());
				SSOAction[] actions = convertToSSOActions(authRole.getActions());
				actionsMap.put(ssoRole, actions);
			}
			Map<String, String> preferences = Collections.emptyMap();
			ssoUser = new SSOUser(username, actionsMap, preferences);
			ssoUser.setSessionId(String
					.valueOf(authRoles.get(0).getSessionId()));
		} else {
			ssoUser = null;
		}
		return ssoUser;
	}

	protected SSOAction[] convertToSSOActions(List<AuthAction> authActions) {
		SSOAction[] actions = new SSOAction[authActions.size()];
		for (int i = 0; i < authActions.size(); i++) {
			AuthAction authAction = authActions.get(i);
			SSOAction ssoAction = new SSOAction(authAction.getActionName(),
					authAction.isLogAction());
			actions[i] = ssoAction;
		}
		return actions;
	}

	public void saveSystemData(String subsystemIdentifier,
			ActionDescription[] actionDescriptions) {
		List<ActionToSave> actions = convertActionDescriptions(actionDescriptions);
		actionDao.saveActions(subsystemIdentifier, actions);
		logger.debug("Saved actions for subsystem " + subsystemIdentifier
				+ ": " + actions.size());
	}

	private List<ActionToSave> convertActionDescriptions(
			ActionDescription[] actionDescriptions) {
		List<ActionToSave> actions = new ArrayList<ActionToSave>(
				actionDescriptions.length);
		for (ActionDescription description : actionDescriptions) {
			ActionToSave action = new ActionToSave();
			action.setName(description.getName());
			action.setDescription(description.getDescription());
			actions.add(action);
		}
		return actions;
	}

	public List<SSOUserWithActions> getUsersWithActions(
			String subsystemIdentifier, String principalName) {
		List<UserWithActions> users = userDao.getUsersAndActions(
				subsystemIdentifier, principalName);
		List<SSOUserWithActions> result = new ArrayList<SSOUserWithActions>(
				users.size());
		for (UserWithActions user : users) {
			result.add(convertToSSOUser(user));
		}
		return result;
	}
	
	public void registerUser(String username, String password, String email,
			long subsystemId, String principalName) {
		RegisterUser registerUser = new RegisterUser();
		registerUser.setUsername(username);
		registerUser.setEmail(email);
		registerUser.setPassword(password);
		registerUser.setPrincipalName(principalName);
		registerUser.setSubsystemId(subsystemId);
        userDao.registerUser(registerUser);
	}

	protected SSOUserWithActions convertToSSOUser(UserWithActions user) {
		return new SSOUserWithActions(user.getUsername(), user.getEmail(),
				convertToSSOActions(user.getActions()));
	}
}
