package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.common.pool.Pool;
import com.payneteasy.superfly.common.pool.SimplePool;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.SmtpServerService;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Properties;

/**
 * @author rpuch
 */
@Slf4j
@Service
public class JavaMailSenderPoolImpl implements JavaMailSenderPool {

    private static final Logger logger = LoggerFactory.getLogger(JavaMailSenderPoolImpl.class);

    private SmtpServerService smtpServerService;

    private final Pool<MessageServerKey, ConfiguredSender> pool = new SimplePool<>() {
        @Override
        protected ConfiguredSender createNew(MessageServerKey key) {
            return createSender(key);
        }
    };

    private ConfiguredSender createSender(MessageServerKey key) {
        final UISmtpServer server;
        if (key.serverId() != null) {
            server = smtpServerService.getSmtpServer(key.serverId());
        } else if (key.subsystemIdentifier() != null) {
            server = smtpServerService.getSmtpServerBySubsystemIdentifier(key.subsystemIdentifier());
        } else {
            server = null;
        }
        if (server != null) {
            server.validate();

            boolean            ssl    = server.isSsl();
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            Properties         props  = new Properties();
            props.put("mail.host", server.getHost());
            props.put("mail.smtp.port", server.getPort() == null ? "25" : String.valueOf(server.getPort()));
            props.put("mail.user", server.getUsername());
            props.put("mail.password", server.getPassword());
            props.put("mail.debug", System.getProperty("mail.debug", "false"));
            props.put(ssl ? "mail.smtps.auth" : "mail.smtp.auth", "true");
            props.put("mail.transport.protocol", ssl ? "smtps" : "smtp");
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(server.getUsername(), server.getPassword());
                }
            });
            sender.setSession(session);
            sender.setProtocol(ssl ? "smtps" : "smtp");
            return new ConfiguredSender(sender, server.getFrom());
        } else {
            logger.warn("No SMTP server found for key {}", key);
            return null;
        }
    }

    @Autowired
    public void setSmtpServerService(SmtpServerService smtpServerService) {
        this.smtpServerService = smtpServerService;
    }

    public ConfiguredSender get(long serverId) {
        MessageServerKey serverKey = MessageServerKey.forMessageServer(serverId);
        log.info("Get server key {}", serverKey);
        ConfiguredSender configuredSender = pool.get(serverKey);
        log.info("Get server configuredSender {}", configuredSender);
        return configuredSender;
    }

    public ConfiguredSender get(String subsystemIdentifier) {
        return pool.get(MessageServerKey.forSubsystem(subsystemIdentifier));
    }

    public void flushAll() {
        pool.flushAll();
    }

    private record MessageServerKey(Long serverId, String subsystemIdentifier) {

        public static MessageServerKey forMessageServer(long serverId) {
                return new MessageServerKey(serverId, null);
            }

            public static MessageServerKey forSubsystem(String identifier) {
                return new MessageServerKey(null, identifier);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                MessageServerKey that = (MessageServerKey) o;

                if (!Objects.equals(serverId, that.serverId)) return false;
                return Objects.equals(subsystemIdentifier, that.subsystemIdentifier);
            }

        @Override
            public String toString() {
                return "MessageServerKey{" +
                        "serverId=" + serverId +
                        ", subsystemIdentifier='" + subsystemIdentifier + '\'' +
                        '}';
            }
        }
}
