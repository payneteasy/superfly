package com.payneteasy.superfly.service.impl;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.payneteasy.superfly.common.pool.Pool;
import com.payneteasy.superfly.common.pool.SimplePool;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.SmtpServerService;

/**
 * @author rpuch
 */
public class JavaMailSenderPoolImpl implements JavaMailSenderPool {

    private static final Logger logger = LoggerFactory.getLogger(JavaMailSenderPoolImpl.class);

    private SmtpServerService smtpServerService;

    private Pool<MessageServerKey, ConfiguredSender> pool = new SimplePool<MessageServerKey, ConfiguredSender>() {
        @Override
        protected ConfiguredSender createNew(MessageServerKey key) {
            return createSender(key);
        }
    };

    private ConfiguredSender createSender(MessageServerKey key) {
        final UISmtpServer server;
        if (key.getServerId() != null) {
            server = smtpServerService.getSmtpServer(key.getServerId());
        } else if (key.getSubsystemIdentifier() != null) {
            server = smtpServerService.getSmtpServerBySubsystemIdentifier(key.getSubsystemIdentifier());
        } else {
            server = null;
        }
        if (server != null) {
            boolean ssl = server.isSsl();
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
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

    @Required
    public void setSmtpServerService(SmtpServerService smtpServerService) {
        this.smtpServerService = smtpServerService;
    }

    public ConfiguredSender get(long serverId) {
        return pool.get(MessageServerKey.forMessageServer(serverId));
    }

    public ConfiguredSender get(String subsystemIdentifier) {
        return pool.get(MessageServerKey.forSubsystem(subsystemIdentifier));
    }

    public void flushAll() {
        pool.flushAll();
    }

    private static class MessageServerKey {
        private final Long serverId;
        private final String subsystemIdentifier;

        private MessageServerKey(Long serverId, String subsystemIdentifier) {
            this.serverId = serverId;
            this.subsystemIdentifier = subsystemIdentifier;
        }

        public static MessageServerKey forMessageServer(long serverId) {
            return new MessageServerKey(serverId, null);
        }

        public static MessageServerKey forSubsystem(String identifier) {
            return new MessageServerKey(null, identifier);
        }

        public Long getServerId() {
            return serverId;
        }

        public String getSubsystemIdentifier() {
            return subsystemIdentifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MessageServerKey that = (MessageServerKey) o;

            if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) return false;
            if (subsystemIdentifier != null ? !subsystemIdentifier.equals(that.subsystemIdentifier) : that.subsystemIdentifier != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = serverId != null ? serverId.hashCode() : 0;
            result = 31 * result + (subsystemIdentifier != null ? subsystemIdentifier.hashCode() : 0);
            return result;
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
