package com.payneteasy.superfly.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.ActionDao;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.hotp.NullHOTPProvider;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthAction;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.UserWithActions;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.spi.HOTPProvider;

@Transactional
public class InternalSSOServiceImpl implements InternalSSOService {

	private static final Logger logger = LoggerFactory.getLogger(InternalSSOServiceImpl.class);

	private UserDao userDao;
	private ActionDao actionDao;
	private NotificationService notificationService;
	private LoggerSink loggerSink;
	private PasswordEncoder passwordEncoder;
	private SaltSource saltSource;
	private HOTPProvider hotpProvider = new NullHOTPProvider();
	private LockoutStrategy lockoutStrategy;

	private AbstractPolicyValidation<PasswordCheckContext> policyValidation;

	@Required
	public void setPolicyValidation(AbstractPolicyValidation<PasswordCheckContext> policyValidation) {
		this.policyValidation = policyValidation;
	}

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Required
	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
	}

	@Required
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Required
	public void setLoggerSink(LoggerSink loggerSink) {
		this.loggerSink = loggerSink;
	}

	@Required
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Required
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

	public void setHOTPProvider(HOTPProvider hotpProvider) {
		this.hotpProvider = hotpProvider;
	}

	@Required
	public void setLockoutStrategy(LockoutStrategy lockoutStrategy) {
		this.lockoutStrategy = lockoutStrategy;
	}

	public SSOUser authenticate(String username, String password, String subsystemIdentifier, String userIpAddress,
			String sessionInfo) {
		SSOUser ssoUser;
		String encPassword = passwordEncoder.encode(password, saltSource.getSalt(username));
		List<AuthRole> authRoles = userDao.authenticate(username, encPassword, subsystemIdentifier, userIpAddress,
				sessionInfo);
		boolean ok = authRoles != null && !authRoles.isEmpty();
		loggerSink.info(logger, "REMOTE_LOGIN", ok, username);
		if (ok) {
			Map<SSORole, SSOAction[]> actionsMap = new HashMap<SSORole, SSOAction[]>(authRoles.size());
			for (AuthRole authRole : authRoles) {
				SSORole ssoRole = new SSORole(authRole.getRoleName());
				SSOAction[] actions = convertToSSOActions(authRole.getActions());
				actionsMap.put(ssoRole, actions);
			}
			Map<String, String> preferences = Collections.emptyMap();
			ssoUser = new SSOUser(username, actionsMap, preferences);
			ssoUser.setSessionId(String.valueOf(authRoles.get(0).getSessionId()));
		} else {
			lockoutStrategy.checkLoginsFailed(username, password);
			ssoUser = null;
		}
		return ssoUser;
	}

	protected SSOAction[] convertToSSOActions(List<AuthAction> authActions) {
		SSOAction[] actions = new SSOAction[authActions.size()];
		for (int i = 0; i < authActions.size(); i++) {
			AuthAction authAction = authActions.get(i);
			SSOAction ssoAction = new SSOAction(authAction.getActionName(), authAction.isLogAction());
			actions[i] = ssoAction;
		}
		return actions;
	}

	public void saveSystemData(String subsystemIdentifier, ActionDescription[] actionDescriptions) {
		List<ActionToSave> actions = convertActionDescriptions(actionDescriptions);
		actionDao.saveActions(subsystemIdentifier, actions);
		if (logger.isDebugEnabled()) {
			logger.debug("Saved actions for subsystem " + subsystemIdentifier + ": " + actions.size());
			logger.debug("Actions are: " + Arrays.asList(actionDescriptions));
		}
	}

	private List<ActionToSave> convertActionDescriptions(ActionDescription[] actionDescriptions) {
		List<ActionToSave> actions = new ArrayList<ActionToSave>(actionDescriptions.length);
		for (ActionDescription description : actionDescriptions) {
			ActionToSave action = new ActionToSave();
			action.setName(description.getName());
			action.setDescription(description.getDescription());
			actions.add(action);
		}
		return actions;
	}

	public List<SSOUserWithActions> getUsersWithActions(String subsystemIdentifier) {
		List<UserWithActions> users = userDao.getUsersAndActions(subsystemIdentifier);
		List<SSOUserWithActions> result = new ArrayList<SSOUserWithActions>(users.size());
		for (UserWithActions user : users) {
			result.add(convertToSSOUser(user));
		}
		return result;
	}

	public void registerUser(String username, String password, String email, String subsystemIdentifier,
			RoleGrantSpecification[] roleGrants, String name, String surname, String secretQuestion, String secretAnswer)
			throws UserExistsException, PolicyValidationException {

		UserRegisterRequest registerUser = new UserRegisterRequest();
		registerUser.setUsername(username);
		registerUser.setEmail(email);
		registerUser.setSalt(saltSource.getSalt(username));
		registerUser.setPassword(passwordEncoder.encode(password, registerUser.getSalt()));
		registerUser.setPrincipalNames(null);
		registerUser.setSubsystemName(subsystemIdentifier);
		registerUser.setName(name);
		registerUser.setSurname(surname);
		registerUser.setSecretQuestion(secretQuestion);
		registerUser.setSecretAnswer(secretAnswer);

		// validate password policy
		policyValidation.validate(new PasswordCheckContext(password, username, passwordEncoder, saltSource, Collections
				.<String> emptyList()));

		RoutineResult result = userDao.registerUser(registerUser);
		if (result.isOk()) {
			for (RoleGrantSpecification roleGrant : roleGrants) {
				result = userDao.grantRolesToUser(
						registerUser.getUserid(),
						roleGrant.isDetectSubsystemIdentifier() ? subsystemIdentifier : roleGrant
								.getSubsystemIdentifier(), roleGrant.getPrincipalName());
				if (!result.isOk()) {
					throw new IllegalStateException("Status: " + result.getStatus() + ", errorMessage: "
							+ result.getErrorMessage());
				}
			}

			notificationService.notifyAboutUsersChanged();
			loggerSink.info(logger, "REGISTER_USER", true, username);
		} else if (result.isDuplicate()) {
			loggerSink.info(logger, "REGISTER_USER", false, username);
			throw new UserExistsException(result.getErrorMessage());
		} else {
			loggerSink.info(logger, "REGISTER_USER", false, username);
			throw new IllegalStateException("Status: " + result.getStatus() + ", errorMessage: "
					+ result.getErrorMessage());
		}
	}

	public boolean authenticateHOTP(String username, String hotp) {
		return hotpProvider.authenticate(username, hotp);
	}

	protected SSOUserWithActions convertToSSOUser(UserWithActions user) {
		return new SSOUserWithActions(user.getUsername(), user.getEmail(), convertToSSOActions(user.getActions()));
	}
}
