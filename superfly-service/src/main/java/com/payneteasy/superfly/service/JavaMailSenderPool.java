package com.payneteasy.superfly.service;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author rpuch
 */
public interface JavaMailSenderPool {
    ConfiguredSender get(long serverId);

    ConfiguredSender get(String subsystemIdentifier);

    void flushAll();
    
    public static class ConfiguredSender {
        private final JavaMailSender sender;
        private final String from;

        public ConfiguredSender(JavaMailSender sender, String from) {
            super();
            this.sender = sender;
            this.from = from;
        }

        public JavaMailSender getSender() {
            return sender;
        }

        public String getFrom() {
            return from;
        }
    }
}
