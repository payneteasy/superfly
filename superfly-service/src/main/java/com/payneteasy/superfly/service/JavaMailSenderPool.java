package com.payneteasy.superfly.service;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author rpuch
 */
public interface JavaMailSenderPool {
    ConfiguredSender get(long serverId);

    ConfiguredSender get(String subsystemIdentifier);

    void flushAll();

    record ConfiguredSender(JavaMailSender sender, String from) {
    }
}
