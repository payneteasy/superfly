package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserWithActions;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;

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
	 * Returns a list of users with actions given to them through a role with
	 * a given principal.
	 * 
	 * @param subsystemName		name of the subsystem from which to obtain info
	 * @return users with actions
	 */
	@AStoredProcedure(name = "list_users")
	List<UserWithActions> getUsersAndActions(String subsystemName);
	
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
	RoutineResult createUser(UIUserForCreate user);

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
	 * @param subsystemIds
	 * 			  comma-separated list of IDs of subsystems which roles
	 * 		      are to be returned (ignored if null)
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_mapped_user_roles_list")
	List<UIRoleForCheckbox> getMappedUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId,
			String subsystemIds);
	
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
	 * @param subsystemIds
	 * 			  comma-separated list of IDs of subsystems which roles
	 * 		      are to be returned (ignored if null)
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_all_user_roles_list")
	List<UIRoleForCheckbox> getAllUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId,
			String subsystemIds);
	
	/**
	 * Returns count of roles for the given user. Both assigned and
	 * not-assigned roles are considered.
	 * 
	 * @param userId
	 *            ID of the user whose roles are to be returned
	 * @param subsystemIds
	 * 			  comma-separated list of IDs of subsystems which roles
	 * 		      are to be returned (ignored if null)
	 * @return roles count
	 */
	@AStoredProcedure(name = "ui_get_all_user_roles_list_count")
	int getAllUserRolesCount(long userId, String subsystemIds);
	
	/**
	 * Returns a list of non-assigned roles for the given user.
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
	 * @param subsystemIds
	 * 			  comma-separated list of IDs of subsystems which roles
	 * 		      are to be returned (ignored if null)
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_unmapped_user_roles_list")
	List<UIRoleForCheckbox> getUnmappedUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId,
			String subsystemIds);
	
	/**
	 * Returns count of non-assigned roles for the given user.
	 * 
	 * @param userId
	 *            ID of the user whose roles are to be returned
	 * @param subsystemIds
	 * 			  comma-separated list of IDs of subsystems which roles
	 * 		      are to be returned (ignored if null)
	 * @return roles count
	 */
	@AStoredProcedure(name = "ui_get_unmapped_user_roles_list_count")
	int getUnmappedUserRolesCount(long userId, String subsystemIds);

	/**
	 * Changes a list of roles assigned to a user.
	 * 
	 * @param userId			ID of the user to change
	 * @param rolesToAddIds		comma-separated list of IDs of roles to be added
	 * @param rolesToRemoveIds	comma-separated list of IDs of roles to be
	 * 							removed
	 * @param rolesToGrantActionsIds comma-separated list of IDs of roles
	 * 							from which all roles will be assigned to user
	 * 							(it must be a subset of rolesToAddIds)
	 * @return routine result
	 */
	@AStoredProcedure(name = "ui_change_user_roles")
	RoutineResult changeUserRoles(long userId, String rolesToAddIds,
			String rolesToRemoveIds, String rolesToGrantActionsIds);

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
	 * @param subsystemIds
	 * 			  IDs of subsystems of interest (if null, all subsystems are
	 * 			  considered)
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions
	 */
	@AStoredProcedure(name = "ui_get_all_user_actions_list")
	List<UIActionForCheckboxForUser> getAllUserActions(int startFrom,
			int recordsCount, int orderFieldNumber, String orderType,
			long userId, String subsystemIds, String actionSubstring);
		
	/**
	 * Returns count of actions for the given user.
	 * 
	 * @param userId
	 *            ID of the user whose actions are to be counted
	 * @param subsystemIds
	 * 			  IDs of subsystems of interest (if null, all subsystems are
	 * 			  considered)            
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions count
	 */
	@AStoredProcedure(name = "ui_get_all_user_actions_list_count")
	int getAllUserActionsCount(long userId, String subsystemIds,
			String actionSubstring);
	
	/**
	 * Returns a list of non-assigned actions for the given user.
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
	 * @param subsystemIds
	 * 			  IDs of subsystems of interest (if null, all subsystems are
	 * 			  considered)
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions
	 */
	@AStoredProcedure(name = "ui_get_unmapped_user_actions_list")
	List<UIActionForCheckboxForUser> getUnmappedUserActions(int startFrom,
			int recordsCount, int orderFieldNumber, String orderType,
			long userId, String subsystemIds, String actionSubstring);
		
	/**
	 * Returns count of non-assigned actions for the given user.
	 * 
	 * @param userId
	 *            ID of the user whose actions are to be counted
	 * @param subsystemIds
	 * 			  IDs of subsystems of interest (if null, all subsystems are
	 * 			  considered)            
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions count
	 */
	@AStoredProcedure(name = "ui_get_unmapped_user_actions_list_count")
	int getUnmappedUserActionsCount(long userId, String subsystemIds,
			String actionSubstring);
	/**
	 * 
	 * @param startFrom
	 * @param recordsCount
	 * @param orderFieldNumber
	 * @param orderType
	 * @param userId
	 * @param subsystemIds
	 * @param actionSubstring
	 * @return
	 */
	@AStoredProcedure(name = "ui_get_mapped_user_actions_list")
	List<UIActionForCheckboxForUser> getMappedUserActions(int startFrom,
			int recordsCount, int orderFieldNumber, String orderType,
			long userId, String subsystemIds, String actionSubstring);
	/**
	 * 
	 * @param userId
	 * @param subsystemIds
	 * @param actionSubstring
	 * @return
	 */
	@AStoredProcedure(name = "ui_get_mapped_user_actions_list_count")
	int getMappedUserActionsCount(long userId, String subsystemIds,
			String actionSubstring);
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
	
	/**
	 * Returns a user with roles assigned to him and actions assigned through
	 * them.
	 * 
	 * @param userId				ID of the user to get
	 * @param subsystemIds			IDs of subsystems to which roles and
	 * 								actions must be restricted (ignored if null)
	 * @param actionNameSubstring	substring which must be contained in the
	 * 								action name to allow action in the list 
	 * @param roleNameSubstring		substring which must be contained in the
	 * 								role name to allow role in the list
	 * @return user with roles and actions
	 */
	@AStoredProcedure(name = "ui_get_user_role_actions")
	UIUserWithRolesAndActions getUserRoleActions(long userId, String subsystemIds,
			String actionNameSubstring, String roleNameSubstring);
	
	/**
	 * 
	 * @param userId
	 * @param roleId
	 * @return
	 */
	@AStoredProcedure(name = "ui_add_subsystem_with_role")
	RoutineResult addSubsystemWithRole(long userId, long roleId);
	
	/**
	 * Registers a user.
	 * 
	 * @param registerUser	user to be registered 
	 * @return routine result
	 */
	@AStoredProcedure(name = "register_user")
	RoutineResult registerUser(UserRegisterRequest registerUser);
}
