package com.payneteasy.superfly.jira.init;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.client.SuperflyDataSender;
import com.payneteasy.superfly.common.store.GroupStoreLocator;
import com.payneteasy.superfly.common.store.UserStore;
import com.payneteasy.superfly.common.store.UserStoreLocator;
import com.payneteasy.superfly.common.utils.StringUtils;
import com.payneteasy.superfly.jira.SuperflyContextLocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that's used to initialize a Superfly-related machinery to allow Jira
 * use it. Main working method is init().
 * 
 * @author Roman Puchkovskiy
 */
public class Initializer {
	
	private SuperflyDataSender sender;
	private SSOService ssoService;
	private String subsystemIdentifier;
	private String groupsCommaDelimitedList;
	
	private UserStoreUpdater userStoreUpdater;
	
	public void setSender(SuperflyDataSender sender) {
		this.sender = sender;
	}

	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
	}

	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}

	public void setGroupsCommaDelimitedList(String groupsCommaDelimitedList) {
		this.groupsCommaDelimitedList = groupsCommaDelimitedList;
	}

	public void init() throws Exception {
		sender.send();
		initSuperflyContext();
		initUsers();
		initGroups();
	}
	
	public void destroy() {
		if (userStoreUpdater != null) {
			userStoreUpdater.cleanup();
		}
	}

	protected void initUsers() {
		getUserStore().setUsers(new ArrayList<SSOUserWithActions>());
		userStoreUpdater.runUpdate();
	}

	protected UserStore getUserStore() {
		return UserStoreLocator.getUserStore();
	}

	protected List<SSOUserWithActions> obtainUsers() {
		return ssoService.getUsersWithActions(subsystemIdentifier);
	}
	
	protected void initGroups() {
		String[] groupNames = obtainGroups();
		List<String> groupNamesList = Arrays.asList(groupNames);
		GroupStoreLocator.getGroupStore().setObjects(groupNamesList);
	}
	
	protected String[] obtainGroups() {
		return StringUtils.commaDelimitedListToStringArray(groupsCommaDelimitedList);
	}
	
	protected void initSuperflyContext() {
		SuperflyContextLocator.getSuperflyContext().setSsoService(ssoService);
		SuperflyContextLocator.getSuperflyContext().setSubsystemIdentifier(subsystemIdentifier);
		userStoreUpdater = new UserStoreUpdater(ssoService, getUserStore(),
				subsystemIdentifier);
		SuperflyContextLocator.getSuperflyContext().setUserStoreUpdater(userStoreUpdater);
	}
}
