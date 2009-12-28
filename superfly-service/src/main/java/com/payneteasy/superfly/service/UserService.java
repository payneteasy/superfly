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
}
