package com.payneteasy.superfly.demo.mock;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.api.UserExistsException;

/**
 * Mock SSOService implementation.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOServiceMock implements SSOService {

	/**
	 * @see SSOService#authenticate(String, String, AuthenticationRequestInfo)
	 */
	public SSOUser authenticate(String username, String password,
			AuthenticationRequestInfo authRequestInfo) {
		Map<SSORole, SSOAction[]> actionsMap = new HashMap<SSORole, SSOAction[]>();
		
		List<SSOAction> adminActions = new ArrayList<SSOAction>();
		adminActions.add(new SSOAction("adminpage1", false));
		adminActions.add(new SSOAction("adminpage2", false));
		actionsMap.put(new SSORole("admin"), adminActions.toArray(new SSOAction[adminActions.size()]));
		
		List<SSOAction> userActions = new ArrayList<SSOAction>();
		userActions.add(new SSOAction("userpage1", false));
		userActions.add(new SSOAction("userpage2", false));
		actionsMap.put(new SSORole("user"), userActions.toArray(new SSOAction[userActions.size()]));
		
		Map<String, String> prefs = Collections.emptyMap();
		
		return new SSOUser(username, Collections.unmodifiableMap(actionsMap), prefs);
	}

	public void sendSystemData(String systemIdentifier,
			ActionDescription[] actionDescriptions) {
	}

	public List<SSOUserWithActions> getUsersWithActions(
			String subsystemIdentifier) {
		List<SSOUserWithActions> users = new ArrayList<SSOUserWithActions>();
		List<SSOAction> actions;
		
		actions = new ArrayList<SSOAction>();
		actions.add(new SSOAction("adminpage1", false));
		actions.add(new SSOAction("adminpage2", false));
		users.add(new SSOUserWithActions("admin", "example@nohost.df",
				actions.toArray(new SSOAction[actions.size()])));
		
		actions = new ArrayList<SSOAction>();
		actions.add(new SSOAction("userpage1", false));
		actions.add(new SSOAction("userpage2", false));
		users.add(new SSOUserWithActions("user", "example@nohost.df",
				actions.toArray(new SSOAction[actions.size()])));
		
		return users;
	}

	public boolean authenticateUsingHOTP(String username, String hotp) {
		return true;
	}

	public void registerUser(String username, String password, String email,
			String subsystemHint, RoleGrantSpecification[] roleGrants,
			String name, String surname, String secretQuestion,
			String secretAnswer) throws UserExistsException,
			PolicyValidationException {
	}

	public void changeTempPassword(String userName, String password) {
		// TODO Auto-generated method stub
		
	}

	public String getFlagTempPassword(String userName) {
		return null;
	}

}
