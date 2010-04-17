package com.payneteasy.superfly.service;

import java.util.Collection;
import java.util.List;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;

/**
 * Service to work with users.
 * 
 * @author Roman Puchkovskiy
 */
public interface UserService {
	/**
	 * Returns users which satisfy to the given conditions.
	 * 
	 * @param userNamePrefix	prefix of the username (ignored if null)
	 * @param roleId			ID of the role which users must have (ignored
	 * 							if null)
	 * @param complectId		ID of the complect which users must have
	 * 							(ignored if null)
	 * @param subsystemId		ID of the subsystem to which user has access
	 * 							(ignored if null)
	 * @param startFrom			user offset
	 * @param recordsCount		user limit
	 * @param orderFieldNumber	number of the field to order by
	 * @param asc				if true, users will be sorted ascendingly
	 * @return users
	 */
	List<UIUserForList> getUsers(String userNamePrefix, Long roleId,
			Long complectId, Long subsystemId, int startFrom,
			int recordsCount, int orderFieldNumber, boolean asc);

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
	int getUsersCount(String userNamePrefix, Long roleId, Long complectId,
			Long subsystemId);
	
	/**
	 * Returns a user for editing.
	 * 
	 * @param userId	ID of the user to return
	 * @return user of null if not found
	 */
	UIUser getUser(long userId);
	
	/**
	 * Creates a user.
	 * 
	 * @param user	user to create
	 */
	RoutineResult createUser(UIUserForCreate user);

	/**
	 * Updates a user.
	 * 
	 * @param user	user to update (username is not changed)
	 */
	RoutineResult updateUser(UIUser user);

	/**
	 * Deleletes a user.
	 * 
	 * @param userId	ID of the user to delete
	 */
	RoutineResult deleteUser(long userId);

	/**
	 * Locks a user.
	 * 
	 * @param userId	ID of the user to lock
	 */
	RoutineResult lockUser(long userId);
	
	/**
	 * Unlocks a user.
	 * 
	 * @param userId	ID of the user to unlock
	 */
	RoutineResult unlockUser(long userId);

	/**
	 * Clones a user.
	 * 
	 * @param templateUserId	ID of the user which will be cloned
	 * @param newUsername		new user's name
	 * @param newPassword		new user's password
	 * @return new user ID
	 */
	long cloneUser(long templateUserId, String newUsername, String newPassword);
	
	/**
	 * Returns a list of roles for the given user. Each role is 'mapped' or
	 * 'unmapped' depending on whether it's assigned this user or not.
	 * 
	 * @param userId
	 *            ID of the user
	 * @param subsystemId
	 * 			  ID of subsystem of interest (if null, all subsystems are
	 * 			  considered)
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @return list of roles
	 */
	List<UIRoleForCheckbox> getAllUserRoles(long userId, Long subsystemId,
			int startFrom, int recordsCount);
	
	/**
	 * Returns count of roles for the given user.
	 * 
	 * @param userId
	 *            ID of the user
	 * @param subsystemId
	 * 			  ID of subsystem of interest (if null, all subsystems are
	 * 			  considered)
	 * @return number of roles
	 */
	int getAllUserRolesCount(long userId, Long subsystemId);
	
	/**
	 * Returns a list of non-assigned roles for the given user.
	 * 
	 * @param userId
	 *            ID of the user
	 * @param subsystemId
	 * 			  ID of subsystem of interest (if null, all subsystems are
	 * 			  considered)
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @return list of roles
	 */
	List<UIRoleForCheckbox> getUnmappedUserRoles(long userId, Long subsystemId,
			int startFrom, int recordsCount);
	
	/**
	 * Returns count of non-assigned roles for the given user.
	 * 
	 * @param userId
	 *            ID of the user
	 * @param subsystemId
	 * 			  ID of subsystem of interest (if null, all subsystems are
	 * 			  considered)
	 * @return number of roles
	 */
	int getUnmappedUserRolesCount(long userId, Long subsystemId);
	
	/**
	 * Changes a list of roles assigned to a user.
	 * 
	 * @param userId			ID of the user to change
	 * @param rolesToAddIds		list of IDs of roles to be added
	 * @param rolesToRemoveIds	list of IDs of roles to be removed
	 * @param rolesToGrantActionsIds list of IDs of roles
	 * 							from which all roles will be assigned to user
	 * 							(it must be a subset of rolesToAddIds)
	 * @return routine result
	 */
	RoutineResult changeUserRoles(long userId, Collection<Long> rolesToAddIds,
			Collection<Long> rolesToRemoveIds,
			Collection<Long> rolesToGrantActionsIds);
	
	/**
	 * Returns a list of actions for the given user.
	 *
	 * @param userId
	 *            ID of the user whose actions are to be returned
	 * @param subsystemId
	 * 			  ID of subsystem of interest (if null, all subsystems are
	 * 			  considered)
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @return actions
	 */
	List<UIActionForCheckboxForUser> getAllUserActions(long userId,
			Long subsystemId, String actionSubstring, int startFrom,
			int recordsCount);
	
	/**
	 * Returns count of actions for the given user.
	 * 
	 * @param userId
	 *            ID of the user whose actions are to be counted
	 * @param subsystemId
	 * 			  ID of subsystems of interest (if null, all subsystems are
	 * 			  considered)
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions count
	 */
	int getAllUserActionsCount(long userId, Long subsystemId, String actionSubstring);
	
	/**
	 * Returns a list of non-assigned actions for the given user.
	 *
	 * @param userId
	 *            ID of the user whose actions are to be returned
	 * @param subsystemId
	 * 			  ID of subsystem of interest (if null, all subsystems are
	 * 			  considered)
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @return actions
	 */
	List<UIActionForCheckboxForUser> getUnmappedUserActions(long userId,
			Long subsystemId, String actionSubstring, int startFrom,
			int recordsCount);
	
	/**
	 * Returns count of non-assigned actions for the given user.
	 * 
	 * @param userId
	 *            ID of the user whose actions are to be counted
	 * @param subsystemId
	 * 			  ID of subsystems of interest (if null, all subsystems are
	 * 			  considered)
	 * @param actionSubstring
	 * 			  substring which must be inside action name (ignored if null)
	 * @return actions count
	 */
	int getUnmappedUserActionsCount(long userId, Long subsystemId, String actionSubstring);
	/**
	 * 
	 * @param userId
	 * @param subsystemId
	 * @param actionSubstring
	 * @param startFrom
	 * @param recordsCount
	 * @return
	 */
	List<UIActionForCheckboxForUser> getMappedUserActions(long userId,
			Long subsystemId, String actionSubstring, int startFrom,
			int recordsCount);
	/**
	 * 
	 * @param userId
	 * @param subsystemId
	 * @param actionSubstring
	 * @return
	 */
	int getMappedUserActionsCount(long userId, Long subsystemId, String actionSubstring);
	/**
	 * Changes a list of actions assigned to a user.
	 * 
	 * @param userId			ID of the user to change
	 * @param roleActionToAddIds	list of IDs of action+roles to be added
	 * @param roleActionToRemoveIds	list of IDs of action+roles to be removed
	 */
	RoutineResult changeUserRoleActions(long userId, Collection<Long> roleActionToAddIds,
			Collection<Long> roleActionToRemoveIds);
	
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
	UIUserWithRolesAndActions getUserRoleActions(long userId, String subsystemIds,
			String actionNameSubstring, String roleNameSubstring);
	/**
	 * 
	 * @param userId
	 * @param roleId
	 * @return
	 */
	RoutineResult addSubsystemWithRole(long userId, long roleId);
}
