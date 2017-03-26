package com.payneteasy.superfly.common.store;

import java.util.Collection;
import java.util.List;

import com.payneteasy.superfly.api.SSOUserWithActions;

/**
 * Store for users.
 * 
 * @author Roman Puchkovskiy
 */
public interface UserStore {
    /**
     * Returns a user by username.
     *
     * @param username    username
     * @return user with the given name or null if not found
     */
    SSOUserWithActions getUser(String username);
    /**
     * Returns true if a user with the given name exists.
     *
     * @param username    username
     * @return true if exists
     */
    boolean userExists(String username);
    /**
     * Sets users to be stored in this store.
     *
     * @param users    users to store
     */
    void setUsers(List<SSOUserWithActions> users);
    /**
     * Returns an unmodifiable collection if users stored here.
     *
     * @return users
     */
    Collection<SSOUserWithActions> getUsers();
}
