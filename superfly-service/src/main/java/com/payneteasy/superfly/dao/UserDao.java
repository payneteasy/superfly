package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UIUser;
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
	
	/**
	 * Returns a user for editing.
	 * 
	 * @param userId	ID of the user to return
	 * @return user of null if not found
	 */
	@AStoredProcedure(name = "ui_get_user")
	UIUser getUser(long userId);

	/**
	 * Creates a user.
	 * 
	 * @param user	user to create
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_create_user")
	RoutineResult createUser(UIUser user);

	/**
	 * Updates a user.
	 * 
	 * @param user	user to update (username is not changed)
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_update_user")
	RoutineResult updateUser(UIUser user);

	/**
	 * Deletes a user.
	 * 
	 * @param userId	ID of the user to delete
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_delete_user")
	RoutineResult deleteUser(long userId);

	/**
	 * Locks a user.
	 * 
	 * @param userId	ID of the user to lock
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_lock_user")
	RoutineResult lockUser(long userId);
	
	/**
	 * Unlocks a user.
	 * 
	 * @param userId	ID of the user to unlock
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_unlock_user")
	RoutineResult unlockUser(long userId);
}
