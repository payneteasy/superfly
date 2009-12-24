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
import com.payneteasy.superfly.dao.ActionDao;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthAction;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.service.InternalSSOService;

@Transactional
public class InternalSSOServiceImpl implements InternalSSOService {
	
	private static final Logger logger = LoggerFactory.getLogger(InternalSSOServiceImpl.class);
	
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
			String subsystemIdentifier, String userIpAddress,
			String sessionInfo) {
		SSOUser ssoUser;
		List<AuthRole> authRoles = userDao.authenticate(username, password,
				subsystemIdentifier, userIpAddress, sessionInfo);
		if (authRoles != null && !authRoles.isEmpty()) {
			Map<SSORole, SSOAction[]> actionsMap = new HashMap<SSORole, SSOAction[]>(authRoles.size());
			for (AuthRole authRole : authRoles) {
				SSORole ssoRole = new SSORole(authRole.getRoleName());
				SSOAction[] actions = new SSOAction[authRole.getActions().size()];
				for (int i = 0; i < authRole.getActions().size(); i++) {
					AuthAction authAction = authRole.getActions().get(i);
					SSOAction ssoAction = new SSOAction(authAction.getActionName(),
							authAction.isLogAction());
					actions[i] = ssoAction;
				}
				actionsMap.put(ssoRole, actions);
			}
			Map<String, String> preferences = Collections.emptyMap();
			ssoUser = new SSOUser(username, actionsMap, preferences);
		} else {
			ssoUser = null;
		}
		return ssoUser;
	}

	public void saveSystemData(String subsystemIdentifier,
			ActionDescription[] actionDescriptions) {
		List<ActionToSave> actions = convertActionDescriptions(actionDescriptions);
		actionDao.saveActions(subsystemIdentifier, actions);
		logger.debug("Saved actions: " + actions.size());
	}

	private List<ActionToSave> convertActionDescriptions(
			ActionDescription[] actionDescriptions) {
		List<ActionToSave> actions = new ArrayList<ActionToSave>(actionDescriptions.length);
		for (ActionDescription description : actionDescriptions) {
			ActionToSave action = new ActionToSave();
			action.setName(description.getName());
			action.setDescription(description.getDescription());
			actions.add(action);
		}
		return actions;
	}

}
