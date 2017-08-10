package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.*;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.*;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;

/**
 * DAO used to work with users.
 *
 * @author Roman Puchkovskiy
 */
public interface UserDao {
    /**
     * Authenticates a user and, if ok, returns his roles with actions.
     *
     * @param username      username to use
     * @param password      password to use
     * @param subsystemName name of the subsystem used to authenticate
     * @param ipAddress     IP address of the user who logs in
     * @param sessionInfo   session info
     * @return session
     */
    @AStoredProcedure(name = "authenticate")
    AuthSession authenticate(String username, String password, String subsystemName, String ipAddress,
            String sessionInfo);

    /**
     * Returns user's role and action as if he was successfully authenticated.
     *
     * @param username      username to use
     * @param subsystemName name of the subsystem used to use
     * @return session
     */
    @AStoredProcedure(name = "get_user_actions")
    AuthSession pseudoAuthenticate(String username, String subsystemName);

    /**
     * Returns a list of users with actions given to them through a role with a
     * given principal.
     *
     * @param subsystemName name of the subsystem from which to obtain info
     * @return users with actions
     */
    @AStoredProcedure(name = "list_users")
    List<UserWithActions> getUsersAndActions(String subsystemName);

    /**
     * Returns users which satisfy to the given conditions.
     *
     * @param startFrom        user offset
     * @param recordsCount     user limit
     * @param orderFieldNumber number of the field to order by
     * @param orderType        asc/desc
     * @param userNamePrefix   prefix of the username (ignored if null)
     * @param roleId           ID of the role which users must have (ignored if null)
     * @param complectId       ID of the complect which users must have (ignored if null)
     * @param subsystemId      ID of the subsystem to which user has access (ignored if null)
     * @return users
     */
    @AStoredProcedure(name = "ui_get_users_list")
    List<UIUserForList> getUsers(long startFrom, long recordsCount, int orderFieldNumber, String orderType,
            String userNamePrefix, Long roleId, Long complectId, Long subsystemId);

    /**
     * Returns total count of users satisfy to the given conditions.
     *
     * @param userNamePrefix prefix of the username (ignored if null)
     * @param roleId         ID of the role which users must have (ignored if null)
     * @param complectId     ID of the complect which users must have (ignored if null)
     * @param subsystemId    ID of the subsystem to which user has access (ignored if null)
     * @return users count
     */
    @AStoredProcedure(name = "ui_get_users_list_count")
    long getUsersCount(String userNamePrefix, Long roleId, Long complectId, Long subsystemId);

    /**
     * Returns a user for editing.
     *
     * @param userId ID of the user to return
     * @return user of null if not found
     */
    @AStoredProcedure(name = "ui_get_user")
    UIUserDetails getUser(long userId);

    /**
     * Creates a user.
     *
     * @param user user to create
     * @return routine result
     */
    @AStoredProcedure(name = "ui_create_user")
    RoutineResult createUser(UIUserForCreate user);

    /**
     * Updates a user.
     *
     * @param user user to update (username, password, salt are not changed)
     * @return routine result
     */
    @AStoredProcedure(name = "ui_update_user")
    RoutineResult updateUser(UIUser user);

    /**
     * Deletes a user.
     *
     * @param userId ID of the user to delete
     * @return routine result
     */
    @AStoredProcedure(name = "ui_delete_user")
    RoutineResult deleteUser(long userId);

    /**
     * Locks a user.
     *
     * @param userId ID of the user to lock
     * @return routine result
     */
    @AStoredProcedure(name = "ui_lock_user")
    RoutineResult lockUser(long userId);

    /**
     * Unlocks a user.
     *
     * @param userId ID of the user to unlock
     * @return routine result
     */
    @AStoredProcedure(name = "ui_unlock_user")
    RoutineResult unlockUser(long userId);

    /**
     * Unlocks a suspended user.
     *
     * @param userId      ID of the user to unlock
     * @param newPassword new password to set as a temp password
     * @return routine result
     */
    @AStoredProcedure(name = "ui_unlock_suspended_user")
    RoutineResult unlockSuspendedUser(long userId, String newPassword);

    /**
     * Creates a clone of the given user with new name and password.
     *
     * @param cloneUserRequest clone request
     * @return routine result
     */
    @AStoredProcedure(name = "ui_clone_user")
    RoutineResult cloneUser(UICloneUserRequest cloneUserRequest);

    /**
     * Returns a list of roles assigned to the given user.
     *
     * @param startFrom        starting index for paging
     * @param recordsCount     limit for paging
     * @param orderFieldNumber number of field to order by
     * @param orderType        'asc'/'desc'
     * @param userId           ID of the user whose roles are to be returned
     * @param subsystemIds     comma-separated list of IDs of subsystems which roles are to
     *                         be returned (ignored if null)
     * @return roles
     */
    @AStoredProcedure(name = "ui_get_mapped_user_roles_list")
    List<UIRoleForCheckbox> getMappedUserRoles(int startFrom, int recordsCount, int orderFieldNumber, String orderType,
            long userId, String subsystemIds);

    /**
     * Returns a list of roles for the given user. Both assigned and
     * not-assigned roles are returned.
     *
     * @param startFrom        starting index for paging
     * @param recordsCount     limit for paging
     * @param orderFieldNumber number of field to order by
     * @param orderType        'asc'/'desc'
     * @param userId           ID of the user whose roles are to be returned
     * @param subsystemIds     comma-separated list of IDs of subsystems which roles are to
     *                         be returned (ignored if null)
     * @return roles
     */
    @AStoredProcedure(name = "ui_get_all_user_roles_list")
    List<UIRoleForCheckbox> getAllUserRoles(int startFrom, int recordsCount, int orderFieldNumber, String orderType,
            long userId, String subsystemIds);

    /**
     * Returns count of roles for the given user. Both assigned and not-assigned
     * roles are considered.
     *
     * @param userId       ID of the user whose roles are to be returned
     * @param subsystemIds comma-separated list of IDs of subsystems which roles are to
     *                     be returned (ignored if null)
     * @return roles count
     */
    @AStoredProcedure(name = "ui_get_all_user_roles_list_count")
    int getAllUserRolesCount(long userId, String subsystemIds);

    /**
     * Returns a list of non-assigned roles for the given user.
     *
     * @param startFrom        starting index for paging
     * @param recordsCount     limit for paging
     * @param orderFieldNumber number of field to order by
     * @param orderType        'asc'/'desc'
     * @param userId           ID of the user whose roles are to be returned
     * @param subsystemIds     comma-separated list of IDs of subsystems which roles are to
     *                         be returned (ignored if null)
     * @return roles
     */
    @AStoredProcedure(name = "ui_get_unmapped_user_roles_list")
    List<UIRoleForCheckbox> getUnmappedUserRoles(int startFrom, int recordsCount, int orderFieldNumber,
            String orderType, long userId, String subsystemIds);

    /**
     * Returns count of non-assigned roles for the given user.
     *
     * @param userId       ID of the user whose roles are to be returned
     * @param subsystemIds comma-separated list of IDs of subsystems which roles are to
     *                     be returned (ignored if null)
     * @return roles count
     */
    @AStoredProcedure(name = "ui_get_unmapped_user_roles_list_count")
    int getUnmappedUserRolesCount(long userId, String subsystemIds);

    /**
     * Changes a list of roles assigned to a user.
     *
     * @param userId                 ID of the user to change
     * @param rolesToAddIds          comma-separated list of IDs of roles to be added
     * @param rolesToRemoveIds       comma-separated list of IDs of roles to be removed
     * @param rolesToGrantActionsIds comma-separated list of IDs of roles from which all roles will
     *                               be assigned to user (it must be a subset of rolesToAddIds)
     * @return routine result
     */
    @AStoredProcedure(name = "ui_change_user_roles")
    RoutineResult changeUserRoles(long userId, String rolesToAddIds, String rolesToRemoveIds,
            String rolesToGrantActionsIds);

    /**
     * Returns a list of actions for the given user.
     *
     * @param startFrom        starting index for paging
     * @param recordsCount     limit for paging
     * @param orderFieldNumber number of field to order by
     * @param orderType        'asc'/'desc'
     * @param userId           ID of the user whose actions are to be returned
     * @param subsystemIds     IDs of subsystems of interest (if null, all subsystems are
     *                         considered)
     * @param actionSubstring  substring which must be inside action name (ignored if null)
     * @return actions
     */
    @AStoredProcedure(name = "ui_get_all_user_actions_list")
    List<UIActionForCheckboxForUser> getAllUserActions(int startFrom, int recordsCount, int orderFieldNumber,
            String orderType, long userId, String subsystemIds, String actionSubstring);

    /**
     * Returns count of actions for the given user.
     *
     * @param userId          ID of the user whose actions are to be counted
     * @param subsystemIds    IDs of subsystems of interest (if null, all subsystems are
     *                        considered)
     * @param actionSubstring substring which must be inside action name (ignored if null)
     * @return actions count
     */
    @AStoredProcedure(name = "ui_get_all_user_actions_list_count")
    int getAllUserActionsCount(long userId, String subsystemIds, String actionSubstring);

    /**
     * Returns a list of non-assigned actions for the given user.
     *
     * @param startFrom        starting index for paging
     * @param recordsCount     limit for paging
     * @param orderFieldNumber number of field to order by
     * @param orderType        'asc'/'desc'
     * @param userId           ID of the user whose actions are to be returned
     * @param subsystemIds     IDs of subsystems of interest (if null, all subsystems are
     *                         considered)
     * @param actionSubstring  substring which must be inside action name (ignored if null)
     * @return actions
     */
    @AStoredProcedure(name = "ui_get_unmapped_user_actions_list")
    List<UIActionForCheckboxForUser> getUnmappedUserActions(int startFrom, int recordsCount, int orderFieldNumber,
            String orderType, long userId, String subsystemIds, long roleId, String actionSubstring);

    /**
     * Returns count of non-assigned actions for the given user.
     *
     * @param userId          ID of the user whose actions are to be counted
     * @param subsystemIds    IDs of subsystems of interest (if null, all subsystems are
     *                        considered)
     * @param actionSubstring substring which must be inside action name (ignored if null)
     * @return actions count
     */
    @AStoredProcedure(name = "ui_get_unmapped_user_actions_list_count")
    int getUnmappedUserActionsCount(long userId, String subsystemIds, long roleId, String actionSubstring);

    /**
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
    List<UIActionForCheckboxForUser> getMappedUserActions(int startFrom, int recordsCount, int orderFieldNumber,
            String orderType, long userId, String subsystemIds, long roleId, String actionSubstring);

    /**
     * @param userId
     * @param subsystemIds
     * @param actionSubstring
     * @return
     */
    @AStoredProcedure(name = "ui_get_mapped_user_actions_list_count")
    int getMappedUserActionsCount(long userId, String subsystemIds, long roleId, String actionSubstring);

    /**
     * Changes a list of actions assigned to a user.
     *
     * @param userId                ID of the user to change
     * @param roleActionToAddIds    comma-separated list of IDs of action+roles to be added
     * @param roleActionToRemoveIds comma-separated list of IDs of action+roles to be removed
     * @return routine result
     */
    @AStoredProcedure(name = "ui_change_user_role_actions")
    RoutineResult changeUserRoleActions(long userId, String roleActionToAddIds, String roleActionToRemoveIds);

    /**
     * Returns a user with roles assigned to him and actions assigned through
     * them.
     *
     * @param userId              ID of the user to get
     * @param subsystemIds        IDs of subsystems to which roles and actions must be
     *                            restricted (ignored if null)
     * @param actionNameSubstring substring which must be contained in the action name to allow
     *                            action in the list
     * @param roleNameSubstring   substring which must be contained in the role name to allow
     *                            role in the list
     * @return user with roles and actions
     */
    @AStoredProcedure(name = "ui_get_user_role_actions")
    UIUserWithRolesAndActions getUserRoleActions(long userId, String subsystemIds, String actionNameSubstring,
            String roleNameSubstring);

    /**
     * @param userId
     * @param roleId
     * @return
     */
    @AStoredProcedure(name = "ui_add_subsystem_with_role")
    RoutineResult addSubsystemWithRole(long userId, long roleId);

    /**
     * Registers a user.
     *
     * @param registerUser user to be registered
     * @return routine result
     */
    @AStoredProcedure(name = "register_user")
    RoutineResult registerUser(UserRegisterRequest registerUser);

    /**
     * Grants roles with the given principal names in a subsystem with the given
     * name to a user with the given ID.
     *
     * @param userId         ID of the user
     * @param subsystemName  name of a subsystem
     * @param principalNames comma-separated list of principal names
     * @return routine result
     */
    @AStoredProcedure(name = "grant_roles_to_user")
    RoutineResult grantRolesToUser(long userId, String subsystemName, String principalNames);

    /**
     * @param userName        user's name
     * @param maxLoginsFailed maximum logins failed
     * @return routine result
     */
    @AStoredProcedure(name = "login_locked")
    RoutineResult lockoutConditionnally(String userName, long maxLoginsFailed, String lockoutType);

    @AStoredProcedure(name = "get_user_salt")
    String getUserSalt(String userName);

    @AStoredProcedure(name = "update_user_salt")
    void updateUserSalt(String username, String salt);

    @AStoredProcedure(name = "get_user_salt_by_user_id")
    String getUserSaltByUserId(long userId);

    @AStoredProcedure(name = "get_user_password_history_and_current_password")
    List<PasswordSaltPair> getUserPasswordHistoryAndCurrentPassword(String username);

    @AStoredProcedure(name = "update_user_salt_by_user_id")
    void updateUserSaltByUserId(long userId, String salt);

    /**
     * @param userName user name
     * @param password password
     * @return routine result
     */
    @AStoredProcedure(name = "change_temp_password")
    RoutineResult changeTempPassword(String userName, String password);

    /**
     * @param userId   user id
     * @param password generated password
     * @return routine result
     */
    @AStoredProcedure(name = "reset_password")
    RoutineResult resetPassword(long userId, String password);

    @AStoredProcedure(name = "get_users_with_expired_passwords")
    List<User> getUsersWithExpiredPasswords(int days);

    @AStoredProcedure(name = "ui_suspend_user")
    RoutineResult suspendUser(long anyLong);

    /**
     * Returns uses which satisfy to 'suspension' criteria.
     * Now, we suspend users that did not login for N days
     * (by default, it's 90 days).
     *
     * @param days  number of days; if more than this number
     *              of days passed since last user's login
     *              (or creation if no login ever occurred),
     *              the user is subject to suspension
     * @return users to suspend
     */
    @AStoredProcedure(name = "get_users_to_suspend")
    List<User> getUsersToSuspend(int days);

    @AStoredProcedure(name = "clear_hotp_logins_failed")
    void clearHOTPLoginsFailed(String username);

    @AStoredProcedure(name = "increment_hotp_logins_failed")
    void incrementHOTPLoginsFailed(String username);

    @AStoredProcedure(name = "get_user_for_description")
    UserForDescription getUserForDescription(String username);

    @AStoredProcedure(name = "update_user_for_description")
    void updateUserForDescription(UserForDescription user);

    @AStoredProcedure(name = "get_user_statuses")
    List<UserWithStatus> getUserStatuses(String userNames);

    /**
     * Checks user login status. That is: tries to login to the
     * specified subsystem and returns one of the following:
     * Y (login successful, there are some actions)
     * N (no user, password mismatched, user locked or no actions)
     * T (login successful, but user's password has expired)
     *
     * @param username            name of the user
     * @param password            hashed password
     * @param subsystemIdentifier subsystem identifier
     * @return user login status
     */
    @AStoredProcedure(name = "get_user_login_status")
    String getUserLoginStatus(String username, String password, String subsystemIdentifier);

    /**
     * Exchanges subsystem token to SSOUser. After this operation
     * returns, subsystem token is not valid anymore and cannot
     * be used for exchanging.
     *
     * @param subsystemToken subsystem token
     * @return SSOUser or null if token does not exist, expired or
     * already used
     */
    @AStoredProcedure(name = "exchange_subsystem_token")
    AuthSession exchangeSubsystemToken(String subsystemToken);

    /**
     * Makes a user complete.
     *
     * @param username name of the user to complete
     */
    @AStoredProcedure(name = "complete_user")
    void completeUser(String username);

    /**
     * Revokes from a user all his roles and replaces them with a given role.
     *
     * @param username      name of the user to work with
     * @param newRole       role to grant
     * @param subsystemName name of the subsystem we work with
     */
    @AStoredProcedure(name = "change_user_role")
    RoutineResult changeUserRole(String username, String newRole, String subsystemName);
}

