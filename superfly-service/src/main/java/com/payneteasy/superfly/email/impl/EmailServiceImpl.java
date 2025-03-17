package com.payneteasy.superfly.email.impl;

import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.email.RuntimeMessagingException;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.JavaMailSenderPool.ConfiguredSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Setter;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private VelocityEngine velocityEngine;
    private JavaMailSenderPool javaMailSenderPool;
    @Setter
    private String  templatePrefix   = "velocity/";
    @Setter
    private boolean enableHotpEmails = true;

    @Autowired
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    @Autowired
    public void setJavaMailSenderPool(JavaMailSenderPool javaMailSenderPool) {
        this.javaMailSenderPool = javaMailSenderPool;
    }

    @Override
    public void sendHOTPTable(String subsystemIdentifier, String to, String fileName, final byte[] table) throws RuntimeMessagingException {
        if (enableHotpEmails) {
            ConfiguredSender sender = javaMailSenderPool.get(subsystemIdentifier);
            if (sender != null) {
                try {
                    String subject = "Your one-time passwords table";
                    Map<String, Object> model = createEmptyModel();
                    String body = getMessageBody("hotp-table.vm", model);

                    MimeMessage       message = createMessage(sender);
                    MimeMessageHelper helper  = createMultipartHelper(message);
                    initHelper(sender, to, subject, body, helper);
                    addOctetStreamAttachment(helper, fileName, table);
                    sender.sender().send(message);
                } catch (MessagingException | MailException e) {
                    throw new RuntimeMessagingException(e);
                }
            }
        }
    }

    @Override
    public void sendNoPublicKeyMessage(String subsystemIdentifier, String email)
            throws RuntimeMessagingException {
        if (enableHotpEmails) {
            ConfiguredSender sender = javaMailSenderPool.get(subsystemIdentifier);
            if (sender != null) {
                try {
                    String subject = "Your public key is not submitted";
                    Map<String, Object> model = createEmptyModel();
                    String body = getMessageBody("no-public-key.vm", model);

                    MimeMessage message = createMessage(sender);
                    MimeMessageHelper helper = createSimpleHelper(message);
                    initHelper(sender, email, subject, body, helper);
                    sender.sender().send(message);
                } catch (MessagingException | MailException e) {
                    throw new RuntimeMessagingException(e);
                }
            }
        }
    }

    @Override
    public void sendTestMessage(long serverId, String email)
            throws RuntimeMessagingException {
        ConfiguredSender sender = javaMailSenderPool.get(serverId);
        try {
            String subject = "Test message";
            Map<String, Object> model = createEmptyModel();
            String body = getMessageBody("test-message.vm", model);

            MimeMessage message = createMessage(sender);
            MimeMessageHelper helper = createSimpleHelper(message);
            initHelper(sender, email, subject, body, helper);
            sender.sender().send(message);
        } catch (MessagingException | MailException e) {
            throw new RuntimeMessagingException(e);
        }
    }

    private MimeMessageHelper createSimpleHelper(MimeMessage message) throws MessagingException {
        return new MimeMessageHelper(message, false);
    }

    @Override
    public void sendPassword(String subsystemIdentifier, String to, String fileName, byte[] encryptedPasswordBytes) {
        ConfiguredSender sender = javaMailSenderPool.get(subsystemIdentifier);
        if (sender != null) {
            try {
                String subject = "Your new password";
                Map<String, Object> model = createEmptyModel();
                String body = getMessageBody("password.vm", model);

                MimeMessage message = createMessage(sender);
                MimeMessageHelper helper = createMultipartHelper(message);
                initHelper(sender, to, subject, body, helper);
                addOctetStreamAttachment(helper, fileName, encryptedPasswordBytes);
                sender.sender().send(message);
            } catch (MessagingException | MailException e) {
                throw new RuntimeMessagingException(e);
            }
        }
    }

    private void addOctetStreamAttachment(MimeMessageHelper helper, String fileName,
            byte[] data) throws MessagingException {
        helper.addAttachment(fileName + ".pgp", new ByteArrayResource(data),
                "application/octet-stream");
    }

    private void initHelper(ConfiguredSender sender, String to, String subject, String body,
            MimeMessageHelper helper) throws MessagingException {
        helper.setFrom(new InternetAddress(sender.from()));
        helper.setSubject(subject);
        helper.setText(body, false);
        helper.setTo(new InternetAddress(to));
    }

    private MimeMessageHelper createMultipartHelper(MimeMessage message) throws MessagingException {
        return new MimeMessageHelper(message, true);
    }

    private MimeMessage createMessage(ConfiguredSender sender) {
        return sender.sender().createMimeMessage();
    }

    private HashMap<String, Object> createEmptyModel() {
        return new HashMap<>();
    }

    private String getMessageBody(String template, Map<String, Object> model) {
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                templatePrefix + template, "utf-8", model);
    }
}
