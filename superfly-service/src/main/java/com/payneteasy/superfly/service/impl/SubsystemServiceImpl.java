package com.payneteasy.superfly.service.impl;


import java.util.List;

import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.*;
import com.payneteasy.superfly.service.impl.remote.check.KeyPairData;
import com.payneteasy.superfly.service.impl.remote.check.RemoteAuthEncryptionAlgorithm;
import com.payneteasy.superfly.utils.SecureTokens;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemView;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;

@Service
@Transactional
public class SubsystemServiceImpl implements SubsystemService {

    private static final Logger logger = LoggerFactory.getLogger(SubsystemServiceImpl.class);

    private SubsystemDao subsystemDao;
    private NotificationService notificationService;
    private LoggerSink loggerSink;
    private JavaMailSenderPool javaMailSenderPool;
    private RemoteAuthCryptoService remoteAuthCryptoService;
    private SubsystemOriginCache subsystemOriginCache;
    private CryptoService cryptoService;

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Autowired
    public void setSubsystemOriginCache(SubsystemOriginCache subsystemOriginCache) {
        this.subsystemOriginCache = subsystemOriginCache;
    }

    @Autowired
    public void setCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    public void setRemoteAuthCryptoService(RemoteAuthCryptoService remoteAuthCryptoService) {
        this.remoteAuthCryptoService = remoteAuthCryptoService;
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
        if (subsystem.getSubsystemToken() == null) {
            // the raw token is not recoverable here: the admin regenerates it on the edit page
            subsystem.setSubsystemToken(SubsystemTokenHasher.hash(SecureTokens.generate("")));
        }
        // the model is page state: it must never hold the plain key after the call
        subsystem.setPrivateKey(encryptKey(subsystem.getPrivateKey()));
        RoutineResult result = subsystemDao.createSubsystem(subsystem);
        invalidateOriginCache();
        loggerSink.info(logger, "CREATE_SUBSYSTEM", true, subsystem.getName());
        javaMailSenderPool.flushAll(); // clearing pool so changes are applied
        return result;
    }

    public RoutineResult deleteSubsystem(Long subsystemId) {
        RoutineResult result = subsystemDao.deleteSubsystem(subsystemId);
        invalidateOriginCache();
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
        // the model is page state: it must never hold the plain key after the call
        subsystem.setPrivateKey(encryptKey(subsystem.getPrivateKey()));
        RoutineResult result = subsystemDao.updateSubsystem(subsystem);
        invalidateOriginCache();
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

    public UISubsystemView getSubsystem(long subsystemId) {
        return subsystemDao.getSubsystem(subsystemId);
    }

    public UISubsystem getSubsystemByName(String subsystemName) {
        return subsystemDao.getSubsystemByName(subsystemName);
    }

    @Override
    public SubsystemAuth getSubsystemAuth(String subsystemName) {
        return subsystemDao.getSubsystemAuth(subsystemName);
    }

    @Override
    public String getSubsystemPrivateKey(String subsystemName) {
        String stored = subsystemDao.getSubsystemPrivateKey(subsystemName);
        if (stored == null || stored.isEmpty()) {
            return null;
        }
        if (cryptoService.isLegacy(stored)) {
            // transitional: the startup task (SubsystemPrivateKeyEncryptionTask) has not encrypted this key yet
            logger.warn("Private key of subsystem {} is not encrypted yet, using it as is", subsystemName);
            return stored;
        }
        try {
            return cryptoService.decrypt(stored);
        } catch (DecryptException e) {
            throw new IllegalStateException("Cannot decrypt the private key of subsystem " + subsystemName, e);
        }
    }

    // null means "no key" (a null in the edit procedure keeps the stored key); an encrypted key is passed through
    private String encryptKey(String plainKey) {
        if (plainKey == null || !cryptoService.isLegacy(plainKey)) {
            return plainKey;
        }
        try {
            return cryptoService.encrypt(plainKey);
        } catch (EncryptException e) {
            throw new IllegalStateException("Cannot encrypt the subsystem private key", e);
        }
    }

    @Override
    public SubsystemTokenData issueSubsystemTokenIfCanLogin(long ssoSessionId, String subsystemIdentifier) {
        return subsystemDao.issueSubsystemTokenIfCanLogin(ssoSessionId,
                subsystemIdentifier, generateUniqueSubsystemToken());
    }

    // After commit: otherwise a concurrent reload could re-cache the pre-commit state for the whole TTL.
    private void invalidateOriginCache() {
        if (subsystemOriginCache == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    subsystemOriginCache.invalidate();
                }
            });
        } else {
            subsystemOriginCache.invalidate();
        }
    }

    private String generateUniqueSubsystemToken() {
        return SecureTokens.generate("ST-");
    }

    @Override
    public String generateMainSubsystemToken(UISubsystem subsystem) {
        String token = SecureTokens.generate("");
        subsystem.setSubsystemToken(SubsystemTokenHasher.hash(token));
        loggerSink.info(logger, "GENERATE_SUBSYSTEM_TOKEN", true, String.valueOf(subsystem.getName()));
        return token;
    }

    @Override
    public KeyPairData generateKeyPair(RemoteAuthEncryptionAlgorithm algorithm) {
        // encrypted right away: the pages keep the pair in their model until the form is submitted
        KeyPairData pair = remoteAuthCryptoService.generateKeyPair(algorithm);
        return new KeyPairData(pair.publicKey(), encryptKey(pair.privateKey()));
    }
}
