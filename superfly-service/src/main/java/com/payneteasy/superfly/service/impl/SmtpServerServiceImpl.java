package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SmtpServerDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SmtpServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author rpuch
 */
public class SmtpServerServiceImpl implements SmtpServerService {
    private static final Logger logger = LoggerFactory.getLogger(SmtpServerServiceImpl.class);

    private SmtpServerDao smtpServerDao;
    private LoggerSink loggerSink;
    private JavaMailSenderPool javaMailSenderPool;

    @Required
    public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
        this.smtpServerDao = smtpServerDao;
    }

    @Required
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Required
    public void setJavaMailSenderPool(JavaMailSenderPool javaMailSenderPool) {
        this.javaMailSenderPool = javaMailSenderPool;
    }

    public List<UISmtpServerForList> listSmtpServers() {
        return smtpServerDao.listSmtpServers();
    }

    public UISmtpServer getSmtpServer(long id) {
        return smtpServerDao.getSmtpServer(id);
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
