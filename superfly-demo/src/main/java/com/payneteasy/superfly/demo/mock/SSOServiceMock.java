package com.payneteasy.superfly.demo.mock;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;

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

}
