package com.payneteasy.superfly.service;

/**
 * Supplies info about the currently logged-in user.
 * 
 * @author Roman Puchkovskiy
 */
public interface UserInfoService {
    /**
     * Returns name of the currently logged-in user.
     *
     * @return current user name
     */
    String getUsername();

    /**
     * Returns address of the client of the current request.
     *
     * @return client IP, or null if there is no current request
     */
    default String getRemoteAddress() {
        return null;
    }
}
