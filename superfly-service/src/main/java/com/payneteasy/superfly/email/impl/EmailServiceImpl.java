package com.payneteasy.superfly.email.impl;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.email.RuntimeMessagingException;

public class EmailServiceImpl implements EmailService {
	
	private JavaMailSender javaMailSender;
	private VelocityEngine velocityEngine;
	private String from;
	
	private String templatePrefix = "velocity/";

	@Required
	public void setJavaMailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@Required
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	@Required
	public void setFrom(String from) {
		this.from = from;
	}

	public void sendHOTPTable(String to, String fileName, final byte[] table) throws RuntimeMessagingException {
		try {
			String subject = "Your one-time passwords table";
			Map<String, Object> model = new HashMap<String, Object>();
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(new InternetAddress(from));
	//		logger.info("Trying to send a message to address " + address + ", subject is " + subject);
			helper.setSubject(subject);
			String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
					templatePrefix + "hotp-table.vm", "utf-8", model);
			helper.setText(body, false);
			helper.setTo(new InternetAddress(to));
			helper.addAttachment(fileName + ".pgp", new ByteArrayResource(table),
					"application/octet-stream");
			javaMailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeMessagingException(e);
		}
	}

	public void sendNoPublicKeyMessage(String email)
			throws RuntimeMessagingException {
		try {
			String subject = "Your public key is not submitted";
			Map<String, Object> model = new HashMap<String, Object>();
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false);
			helper.setFrom(new InternetAddress(from));
			helper.setSubject(subject);
			String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
					templatePrefix + "no-public-key.vm", "utf-8", model);
			helper.setText(body, false);
			helper.setTo(new InternetAddress(email));
			javaMailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeMessagingException(e);
		}
	}

}
