package com.payneteasy.superfly.service.impl;


import java.util.List;
import java.util.UUID;

import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.utils.RandomGUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SubsystemService;

@Service
@Transactional
public class SubsystemServiceImpl implements SubsystemService {

    private static final Logger logger = LoggerFactory.getLogger(SubsystemServiceImpl.class);

    private SubsystemDao subsystemDao;
    private NotificationService notificationService;
    private LoggerSink loggerSink;
    private JavaMailSenderPool javaMailSenderPool;

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Autowired
    public void setJavaMailSenderPool(JavaMailSenderPool javaMailSenderPool) {
        this.javaMailSenderPool = javaMailSenderPool;
    }

    public RoutineResult createSubsystem(UISubsystem subsystem) {
        subsystem.setSubsystemToken(generateMainSubsystemToken());
        RoutineResult result = subsystemDao.createSubsystem(subsystem);
        loggerSink.info(logger, "CREATE_SUBSYSTEM", true, subsystem.getName());
        javaMailSenderPool.flushAll(); // clearing pool so changes are applied
        return result;
    }

    public RoutineResult deleteSubsystem(Long subsystemId) {
        RoutineResult result = subsystemDao.deleteSubsystem(subsystemId);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "DELETE_SUBSYSTEM", result.isOk(), String.valueOf(subsystemId));
        javaMailSenderPool.flushAll(); // clearing pool so changes are applied
        return result;
    }

    public List<UISubsystemForList> getSubsystems() {
        return subsystemDao.getSubsystems();
    }

    public RoutineResult updateSubsystem(UISubsystem subsystem) {
        RoutineResult result = subsystemDao.updateSubsystem(subsystem);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "UPDATE_SUBSYSTEM", result.isOk(), subsystem.getName());
        javaMailSenderPool.flushAll(); // clearing pool so changes are applied
        return result;
    }

    public List<UISubsystemForFilter> getSubsystemsForFilter() {
        return subsystemDao.getSubsystemsForFilter();
    }

    public UISubsystem getSubsystem(long subsystemId) {
        return subsystemDao.getSubsystem(subsystemId);
    }

    public UISubsystem getSubsystemByName(String subsystemName) {
        return subsystemDao.getSubsystemByName(subsystemName);
    }

    @Override
    public SubsystemTokenData issueSubsystemTokenIfCanLogin(long ssoSessionId, String subsystemIdentifier) {
        return subsystemDao.issueSubsystemTokenIfCanLogin(ssoSessionId,
                subsystemIdentifier, generateUniqueSubsystemToken());
    }

    private String generateUniqueSubsystemToken() {
        return "ST-" + new RandomGUID().toString().replaceAll("-", "");
    }

    @Override
    public String generateMainSubsystemToken() {
        return UUID.randomUUID().toString();
    }
}
