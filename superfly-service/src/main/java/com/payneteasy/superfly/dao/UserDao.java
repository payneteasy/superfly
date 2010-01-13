package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
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
	 * @param subsystemId		ID of the subsystem to which user has access
	 * 							(ignored if null)
	 * @return users
	 */
	@AStoredProcedure(name = "ui_get_users_list")
	List<UIUserForList> getUsers(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType,
			String userNamePrefix, Long roleId, Long complectId,
			Long subsystemId);

	/**
	 * Returns total count of users satisfy to the given conditions.
	 * 
	 * @param userNamePrefix	prefix of the username (ignored if null)
	 * @param roleId			ID of the role which users must have (ignored
	 * 							if null)
	 * @param complectId		ID of the complect which users must have
	 * 							(ignored if null)
	 * @param subsystemId		ID of the subsystem to which user has access
	 * 							(ignored if null)
	 * @return users count
	 */
	@AStoredProcedure(name = "ui_get_users_list_count")
	int getUsersCount(String userNamePrefix, Long roleId, Long complectId,
			Long subsystemId);
	
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
	
	/**
	 * Creates a clone of the given user with new name and password.
	 *
	 * @param cloneUserRequest	clone request
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_clone_user")
	RoutineResult cloneUser(UICloneUserRequest cloneUserRequest);
	
	/**
	 * Returns a list of roles assigned to the given user.
	 * 
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @param orderFieldNumber
	 *            number of field to order by
	 * @param orderType
	 *            'asc'/'desc'
	 * @param userId
	 *            ID of the user whose roles are to be returned
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_mapped_user_roles_list")
	List<UIRoleForCheckbox> getMappedUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId);
	
	/**
	 * Returns a list of roles for the given user. Both assigned and
	 * not-assigned roles are returned.
	 * 
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @param orderFieldNumber
	 *            number of field to order by
	 * @param orderType
	 *            'asc'/'desc'
	 * @param userId
	 *            ID of the user whose roles are to be returned
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_all_user_roles_list")
	List<UIRoleForCheckbox> getAllUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId);

	/**
	 * Changes a list of roles assigned to a user.
	 * 
	 * @param userId			ID of the user to change
	 * @param rolesToAddIds		comma-separated list of IDs of roles to be added
	 * @param rolesToRemoveIds	comma-separated list of IDs of roles to be
	 * 							removed
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_change_user_roles")
	RoutineResult changeUserRoles(long userId, String rolesToAddIds,
			String rolesToRemoveIds);

	/**
	 * Returns a list of actions for the given user.
	 * 
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @param orderFieldNumber
	 *            number of field to order by
	 * @param orderType
	 *            'asc'/'desc'
	 * @param userId
	 *            ID of the user whose actions are to be returned
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions
	 */
	@AStoredProcedure(name = "ui_get_all_user_actions_list")
	List<UIActionForCheckboxForUser> getAllUserActions(int startFrom,
			int recordsCount, int orderFieldNumber, String orderType,
			long userId, String subsystemIds,String actionSubstring);
	
	/**
	 * Returns count of actions for the given user.
	 * 
	 * @param userId
	 *            ID of the user whose actions are to be counted
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions count
	 */
	@AStoredProcedure(name = "ui_get_all_user_actions_list_count")
	int getAllUserActionsCount(long userId, String subsystemIds, String actionSubstring);
	
	/**
	 * Changes a list of actions assigned to a user.
	 * 
	 * @param userId			ID of the user to change
	 * @param roleActionToAddIds	comma-separated list of IDs of action+roles
	 * 								to be added
	 * @param roleActionToRemoveIds	comma-separated list of IDs of action+roles
	 * 								to be removed
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_change_user_role_actions")
	RoutineResult changeUserRoleActions(long userId, String roleActionToAddIds,
			String roleActionToRemoveIds);
}
