package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForList;

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
	 * @param startFrom			user offset
	 * @param recordsCount		user limit
	 * @param orderFieldNumber	number of the field to order by
	 * @param asc				if true, users will be sorted ascendingly
	 * @return users
	 */
	List<UIUserForList> getUsers(String userNamePrefix, Long roleId,
			Long complectId, int startFrom,
			int recordsCount, int orderFieldNumber, boolean asc);

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
	int getUsersCount(String userNamePrefix, Long roleId, Long complectId);
	
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
	void createUser(UIUser user);

	/**
	 * Updates a user.
	 * 
	 * @param user	user to update (username is not changed)
	 */
	void updateUser(UIUser user);

	/**
	 * Deleletes a user.
	 * 
	 * @param userId	ID of the user to delete
	 */
	void deleteUser(long userId);

	/**
	 * Locks a user.
	 * 
	 * @param userId	ID of the user to lock
	 */
	void lockUser(long userId);
	
	/**
	 * Unlocks a user.
	 * 
	 * @param userId	ID of the user to unlock
	 */
	void unlockUser(long userId);

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
	 * Changes a list of roles assigned to a user.
	 * 
	 * @param userId			ID of the user to change
	 * @param rolesToAddIds		list of IDs of roles to be added
	 * @param rolesToRemoveIds	list of IDs of roles to be removed
	 * @return routine result
	 */
	void changeUserRoles(long userId, List<Long> rolesToAddIds,
			List<Long> rolesToRemoveIds);
}
