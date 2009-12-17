package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.AuthRole;

/**
 * DAO used to work with users.
 * 
 * @author Roman Puchkovskiy
 */
public interface UserDao {
	/**
	 * Authenticates a user and, if ok, returns his roles with actions.
	 * 
	 * @param username		username to use
	 * @param password		password to use
	 * @param subsystemName	name of the subsystem used to authenticate
	 * @param ipAddress		IP address of the user who logs in
	 * @param sessionInfo	session info
	 * @return roles with actions
	 */
	@AStoredProcedure(name = "get_user_actions")
	List<AuthRole> authenticate(String username, String password,
			String subsystemName, String ipAddress, String sessionInfo);
}
