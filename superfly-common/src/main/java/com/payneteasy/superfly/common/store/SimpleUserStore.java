package com.payneteasy.superfly.common.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.payneteasy.superfly.api.SSOUserWithActions;

/**
 * Simple {@link UserStore} implementation which uses an in-memory map.
 * 
 * @author Roman Puchkovskiy
 */
public class SimpleUserStore implements UserStore {

    private Map<String, SSOUserWithActions> usersMap;

    public SSOUserWithActions getUser(String username) {
        return usersMap.get(username);
    }

    public boolean userExists(String username) {
        return usersMap.containsKey(username);
    }

    public void setUsers(List<SSOUserWithActions> users) {
        usersMap = new HashMap<String, SSOUserWithActions>();
        for (SSOUserWithActions user : users) {
            usersMap.put(user.getName(), user);
        }
        usersMap = Collections.unmodifiableMap(usersMap);
    }

    public Collection<SSOUserWithActions> getUsers() {
        return usersMap.values();
    }

}
