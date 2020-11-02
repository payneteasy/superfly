package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.RoleDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.role.UIRoleForView;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.utils.StringUtils;

@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    private RoleDao roleDao;
    private NotificationService notificationService;
    private LoggerSink loggerSink;

    @Required
    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Required
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Required
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    public List<UIRoleForFilter> getRolesForFilter() {
        return roleDao.getRolesForFilter(null, null, 0, Integer.MAX_VALUE);
    }

    public long getRoleCount(String rolesName, List<Long> subsystems) {
        return roleDao.getRoleCount(rolesName, StringUtils
                .collectionToCommaDelimitedString(subsystems));
    }

    public List<UIRoleForList> getRoles(long startFrom, long recordsCount,
            int orderFieldNumber, boolean asc, String rolesName,
            List<Long> subsystems) {
        return roleDao.getRoles(startFrom, recordsCount, orderFieldNumber,
                asc ? DaoConstants.ASC : DaoConstants.DESC, rolesName,
                StringUtils.collectionToCommaDelimitedString(subsystems));
    }

    public RoutineResult deleteRole(long roleId) {
        RoutineResult result = roleDao.deleteRole(roleId);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "DELETE_ROLE", result.isOk(), String.valueOf(roleId));
        return result;
    }

    public UIRoleForView getRole(long roleId) {
        return roleDao.getRole(roleId);
    }

    public RoutineResult updateRole(UIRole role) {
        RoutineResult result = roleDao.updateRole(role);
        // notifying as principal could be changed, and actions for users
        // 'via principal' could be changed too
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "UPDATE_ROLE", result.isOk(), role.getRoleName() + " in " + role.getSubsystemId());
        return result;
    }

    public RoutineResult createRole(UIRole role) {
        RoutineResult result = roleDao.createRole(role);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "CREATE_ROLE", result.isOk(), role.getRoleName() + " in " + role.getSubsystemId());
        return result;
    }

    public RoutineResult changeRoleGroups(long roleId,
            List<Long> groupToAddIds, List<Long> groupToRemoveIds) {
        RoutineResult result = roleDao.changeRoleGroups(roleId,
                StringUtils.collectionToCommaDelimitedString(groupToAddIds),
                StringUtils.collectionToCommaDelimitedString(groupToRemoveIds));
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "CHANGE_ROLE_GROUPS", result.isOk(), String.valueOf(roleId));
        return result;

    }

    public RoutineResult changeRoleActions(long roleId,
            List<Long> actionToAddIds, List<Long> actionToRemoveIds) {
        RoutineResult result = roleDao.changeRoleActions(roleId,
                StringUtils.collectionToCommaDelimitedString(actionToAddIds),
                StringUtils.collectionToCommaDelimitedString(actionToRemoveIds));
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "CHANGE_ROLE_ACTIONS", result.isOk(), String.valueOf(roleId));
        return result;
    }

    public List<UIActionForCheckboxForRole> getAllRoleActions(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
            long roleId, String actionName) {
        return roleDao.getAllRoleActions(startFrom, recordsCount,
                orderFieldNumber, ascending ? DaoConstants.ASC
                        : DaoConstants.DESC, roleId, actionName);
    }

    public long getAllRoleActionsCount(long roleId, String actionName) {
        return roleDao.getAllRoleActionsCount(roleId, actionName);
    }

    public List<UIActionForCheckboxForRole> getMappedRoleActions(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
            long roleId, String actionName) {
        return roleDao.getMappedRoleActions(startFrom, recordsCount,
                orderFieldNumber, ascending ? DaoConstants.ASC
                        : DaoConstants.DESC, roleId, actionName);
    }

    public long getMappedRoleActionsCount(long roleId, String actionName) {
        return roleDao.getMappedRoleActionsCount(roleId, actionName);
    }

    public long getAllRoleGroupsCount(long roleId) {
        return roleDao.getAllRoleGroupsCount(roleId);
    }

    public List<UIGroupForCheckbox> getAllRoleGroups(long startFrom,
            long recordsCount, int orderFieldNumber, String orderType,
            long roleId) {
        return roleDao.getAllRoleGroups(startFrom, recordsCount,
                orderFieldNumber, orderType, roleId);
    }

    public List<UIRoleForFilter> getRolesForCreateUser(List<Long> subId) {

        return roleDao.getRolesForFilter(StringUtils
                .collectionToCommaDelimitedString(subId), null, 0,
                Integer.MAX_VALUE);
    }

    public List<UIActionForCheckboxForRole> getUnMappedRoleActions(
            long startFrom, long recordsCount, int orderFieldNumber,
            boolean ascending, long roleId, String actionName) {

        return roleDao.getUnMappedRoleActions(startFrom, recordsCount,
                orderFieldNumber, ascending ? DaoConstants.ASC
                        : DaoConstants.DESC, roleId, actionName);
    }

    public List<UIGroupForCheckbox> getMappedRoleGroups(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
            long roleId) {
        return roleDao.getMappedRoleGroups(startFrom, recordsCount, orderFieldNumber, ascending ? DaoConstants.ASC: DaoConstants.DESC, roleId);
    }

    @Override
    public int getMappedRoleGroupsCount(long roleId) {
        return roleDao.getMappedRoleGroupsCount(roleId);
    }


    public List<UIGroupForCheckbox> getUnMappedRoleGroups(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
            long roleId) {
        return roleDao.getUnMappedRoleGroups(startFrom, recordsCount, orderFieldNumber, ascending ? DaoConstants.ASC
                : DaoConstants.DESC, roleId);
    }

}
