package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;
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

    // smtp_servers.password
    private static final int MAX_STORED_PASSWORD_LENGTH = 255;

    private SmtpServerDao smtpServerDao;
    private LoggerSink loggerSink;
    private JavaMailSenderPool javaMailSenderPool;
    private CryptoService cryptoService;

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

    @Autowired
    public void setCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public List<UISmtpServerForList> listSmtpServers() {
        return smtpServerDao.listSmtpServers();
    }

    public UISmtpServer getSmtpServer(long id) {
        UISmtpServer smtpServer = smtpServerDao.getSmtpServer(id);
        log.info("Get smtpServer from DAO {}", smtpServer);
        if (smtpServer != null) {
            smtpServer.setPassword(null);
        }
        return smtpServer;
    }

    public UISmtpServer getSmtpServerWithPassword(long id) {
        return withDecryptedPassword(smtpServerDao.getSmtpServer(id));
    }

    public UISmtpServer getSmtpServerBySubsystemIdentifier(String subsystemIdentifier) {
        return withDecryptedPassword(smtpServerDao.getSmtpServerBySubsystemIdentifier(subsystemIdentifier));
    }

    public RoutineResult createSmtpServer(UISmtpServer server) {
        RoutineResult result;
        try {
            if (!encryptPassword(server)) {
                return passwordTooLong();
            }
            result = smtpServerDao.createSmtpServer(server);
        } finally {
            server.setPassword(null);
        }
        loggerSink.info(logger, "CREATE_SMTP_SERVER", true, server.getName());
        return result;
    }

    public RoutineResult updateSmtpServer(UISmtpServer server) {
        RoutineResult result;
        try {
            if (!encryptPassword(server)) {
                return passwordTooLong();
            }
            result = smtpServerDao.updateSmtpServer(server);
        } finally {
            server.setPassword(null);
        }
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

    public void logPasswordViewed(String serverName) {
        loggerSink.info(logger, "VIEW_SMTP_PASSWORD", true, serverName);
    }

    // an empty password becomes null, which keeps the stored one on update; false if the ciphertext would not fit
    private boolean encryptPassword(UISmtpServer server) {
        String password = server.getPassword();
        if (password == null || password.isEmpty()) {
            server.setPassword(null);
            return true;
        }
        String encrypted;
        try {
            encrypted = cryptoService.encrypt(password);
        } catch (EncryptException e) {
            throw new IllegalStateException("Cannot encrypt the password of SMTP server " + server.getName(), e);
        }
        if (encrypted.length() > MAX_STORED_PASSWORD_LENGTH) {
            return false;
        }
        server.setPassword(encrypted);
        return true;
    }

    private static RoutineResult passwordTooLong() {
        return new RoutineResult("fail", "The password is too long");
    }

    private UISmtpServer withDecryptedPassword(UISmtpServer server) {
        if (server == null || server.getPassword() == null || server.getPassword().isEmpty()) {
            return server;
        }
        if (cryptoService.isLegacy(server.getPassword())) {
            // transitional: the startup task (SmtpServerPasswordEncryptionTask) has not encrypted this password yet
            logger.warn("Password of SMTP server {} is not encrypted yet, using it as is", server.getName());
            return server;
        }
        try {
            server.setPassword(cryptoService.decrypt(server.getPassword()));
        } catch (DecryptException e) {
            throw new IllegalStateException("Cannot decrypt the password of SMTP server " + server.getName(), e);
        }
        return server;
    }
}
