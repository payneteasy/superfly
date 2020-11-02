package com.payneteasy.superfly.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemToNotify;
import com.payneteasy.superfly.notification.Notifier;
import com.payneteasy.superfly.notification.UsersChangedNotification;
import com.payneteasy.superfly.service.NotificationService;

@Transactional
public class NotificationServiceImpl implements NotificationService {

    private Notifier notifier;
    private SubsystemDao subsystemDao;

    @Required
    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    @Required
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    public void notifyAboutUsersChanged() {
        List<SubsystemToNotify> subsystems = subsystemDao.getSubsystemsAllowingToListUsers();
        List<UsersChangedNotification> notifications = new ArrayList<>(subsystems.size());
        for (SubsystemToNotify subsystem : subsystems) {
            if (subsystem.isSendCallbacks()) {
                UsersChangedNotification notification = new UsersChangedNotification();
                notification.setCallbackUri(subsystem.getCallbackInformation());
                notifications.add(notification);
            }
        }
        notifier.notifyAboutUsersChanged(notifications);
    }

}
