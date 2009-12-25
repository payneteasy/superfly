package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.ui.user.UIUserForList;

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
	
	/**
	 * Returns users which satisfy to the given conditions.
	 * 
	 * @param startFrom			user offset
	 * @param recordsCount		user limit
	 * @param orderFieldNumber	number of the field to order by
	 * @param orderType			asc/desc
	 * @param userNamePrefix	prefix of the username (ignored if null)
	 * @param roleId			ID of the role which users must have (ignored
	 * 							if null)
	 * @param complectId		ID of the complect which users must have
	 * 							(ignored if null)
	 * @return users
	 */
	@AStoredProcedure(name = "ui_get_users_list")
	List<UIUserForList> getUsers(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType,
			String userNamePrefix, Long roleId, Long complectId);

	/**
	 * Returns total count of users satisfy to the given conditions.
	 * 
	 * @param userNamePrefix	prefix of the username (ignored if null)
	 * @param roleId			ID of the role which users must have (ignored
	 * 							if null)
	 * @param complectId		ID of the complect which users must have
	 * 							(ignored if null)
	 * @return users count
	 */
	@AStoredProcedure(name = "ui_get_users_list_count")
	int getUsersCount(String userNamePrefix, Long roleId, Long complectId);
}
