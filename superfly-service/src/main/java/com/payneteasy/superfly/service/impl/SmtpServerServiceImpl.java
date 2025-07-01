package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SmtpServerDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SmtpServerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author rpuch
 */
@Slf4j
@Service
@Transactional
public class SmtpServerServiceImpl implements SmtpServerService {
    private static final Logger logger = LoggerFactory.getLogger(SmtpServerServiceImpl.class);

    private SmtpServerDao smtpServerDao;
    private LoggerSink loggerSink;
    private JavaMailSenderPool javaMailSenderPool;

    @Autowired
    public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
        this.smtpServerDao = smtpServerDao;
    }

    @Autowired
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Autowired
    public void setJavaMailSenderPool(JavaMailSenderPool javaMailSenderPool) {
        this.javaMailSenderPool = javaMailSenderPool;
    }

    public List<UISmtpServerForList> listSmtpServers() {
        return smtpServerDao.listSmtpServers();
    }

    public UISmtpServer getSmtpServer(long id) {
        UISmtpServer smtpServer = smtpServerDao.getSmtpServer(id);
        log.info("Get smtpServer from DAO {}", smtpServer);
        return smtpServer;
    }

    public UISmtpServer getSmtpServerBySubsystemIdentifier(String subsystemIdentifier) {
        return smtpServerDao.getSmtpServerBySubsystemIdentifier(subsystemIdentifier);
    }

    public RoutineResult createSmtpServer(UISmtpServer server) {
        RoutineResult result = smtpServerDao.createSmtpServer(server);
        loggerSink.info(logger, "CREATE_SMTP_SERVER", true, server.getName());
        return result;
    }

    public RoutineResult updateSmtpServer(UISmtpServer server) {
        RoutineResult result = smtpServerDao.updateSmtpServer(server);
        loggerSink.info(logger, "UPDATE_SMTP_SERVER", true, server.getName());
        javaMailSenderPool.flushAll(); // clearing pool so changes are applied
        return result;
    }

    public RoutineResult deleteSmtpServer(long id) {
        RoutineResult result = smtpServerDao.deleteSmtpServer(id);
        loggerSink.info(logger, "DELETE_SMTP_SERVER", true, String.valueOf(id));
        javaMailSenderPool.flushAll(); // clearing pool so changes are applied
        return result;
    }

    public List<UISmtpServerForFilter> getSmtpServersForFilter() {
        return smtpServerDao.getSmtpServersForFilter();
    }
}
