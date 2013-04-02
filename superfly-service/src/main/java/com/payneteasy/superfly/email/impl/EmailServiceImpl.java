package com.payneteasy.superfly.email.impl;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.email.RuntimeMessagingException;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.JavaMailSenderPool.ConfiguredSender;

public class EmailServiceImpl implements EmailService {

	private VelocityEngine velocityEngine;
    private JavaMailSenderPool javaMailSenderPool;
	private String templatePrefix = "velocity/";
    private boolean enableHotpEmails = true;

    @Required
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

    @Required
    public void setJavaMailSenderPool(JavaMailSenderPool javaMailSenderPool) {
        this.javaMailSenderPool = javaMailSenderPool;
    }

    public void setTemplatePrefix(String templatePrefix) {
        this.templatePrefix = templatePrefix;
    }

    public void setEnableHotpEmails(boolean enableHotpEmails) {
        this.enableHotpEmails = enableHotpEmails;
    }

    public void sendHOTPTable(String subsystemIdentifier, String to, String fileName, final byte[] table) throws RuntimeMessagingException {
    	ConfiguredSender sender = javaMailSenderPool.get(subsystemIdentifier);
    	if (sender != null) {
			try {
				String subject = "Your one-time passwords table";
				Map<String, Object> model = new HashMap<String, Object>();
				MimeMessage message = sender.getSender().createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
				helper.setFrom(new InternetAddress(sender.getFrom()));
//	    		logger.info("Trying to send a message to address " + address + ", subject is " + subject);
				helper.setSubject(subject);
				String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
						templatePrefix + "hotp-table.vm", "utf-8", model);
				helper.setText(body, false);
				helper.setTo(new InternetAddress(to));
				helper.addAttachment(fileName + ".pgp", new ByteArrayResource(table),
						"application/octet-stream");
				sender.getSender().send(message);
			} catch (MessagingException e) {
				throw new RuntimeMessagingException(e);
			} catch (MailException e) {
				throw new RuntimeMessagingException(e);
			}
    	}
	}

	public void sendNoPublicKeyMessage(String subsystemIdentifier, String email)
			throws RuntimeMessagingException {
		if (enableHotpEmails) {
            ConfiguredSender sender = javaMailSenderPool.get(subsystemIdentifier);
            if (sender != null) {
                try {
                    String subject = "Your public key is not submitted";
                    Map<String, Object> model = new HashMap<String, Object>();
                    MimeMessage message = sender.getSender().createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, false);
                    helper.setFrom(new InternetAddress(sender.getFrom()));
                    helper.setSubject(subject);
                    String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                            templatePrefix + "no-public-key.vm", "utf-8", model);
                    helper.setText(body, false);
                    helper.setTo(new InternetAddress(email));
                    sender.getSender().send(message);
                } catch (MessagingException e) {
                    throw new RuntimeMessagingException(e);
                } catch (MailException e) {
                    throw new RuntimeMessagingException(e);
                }
            }
        }
	}

	public void sendTestMessage(long serverId, String email)
			throws RuntimeMessagingException {
		ConfiguredSender sender = javaMailSenderPool.get(serverId);
		try {
			String subject = "Test message";
			Map<String, Object> model = new HashMap<String, Object>();
			MimeMessage message = sender.getSender().createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false);
			helper.setFrom(new InternetAddress(sender.getFrom()));
			helper.setSubject(subject);
			String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
					templatePrefix + "test-message.vm", "utf-8", model);
			helper.setText(body, false);
			helper.setTo(new InternetAddress(email));
			sender.getSender().send(message);
		} catch (MessagingException e) {
			throw new RuntimeMessagingException(e);
		} catch (MailException e) {
			throw new RuntimeMessagingException(e);
		}
	}
}
