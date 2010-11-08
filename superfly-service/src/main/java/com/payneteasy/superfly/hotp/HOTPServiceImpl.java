package com.payneteasy.superfly.hotp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.email.RuntimeMessagingException;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPService;

@Transactional
public class HOTPServiceImpl implements HOTPService {
	
	private static final Logger logger = LoggerFactory.getLogger(HOTPServiceImpl.class);
	
	private EmailService emailService;
	private HOTPProvider hotpProvider;
	private PublicKeyCrypto publicKeyCrypto;
	private UserDao userDao;

	@Required
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	@Required
	public void setHotpProvider(HOTPProvider hotpProvider) {
		this.hotpProvider = hotpProvider;
	}

	@Required
	public void setPublicKeyCrypto(PublicKeyCrypto publicKeyCrypto) {
		this.publicKeyCrypto = publicKeyCrypto;
	}

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void sendTableIfSupported(long userId) throws MessageSendException {
		obtainUserIfNeededAndSendTableIfSupported(userId, null);
	}
	
	public void resetTableAndSendIfSupported(long userId) throws MessageSendException {
		UIUser user = userDao.getUser(userId);
		hotpProvider.resetSequence(user.getUsername());
		obtainUserIfNeededAndSendTableIfSupported(userId, user);
	}
	
	private void obtainUserIfNeededAndSendTableIfSupported(long userId, UIUser user) throws MessageSendException {
		if (hotpProvider.outputsSequenceForDownload()) {
			if (user == null) {
				user = userDao.getUser(userId);
			}
			if (user.getPublicKey() != null && user.getPublicKey().trim().length() > 0) {
				actuallySendTable(user);
			} else {
				sendNoPublicKey(user.getEmail());
			}
		}
	}

	protected void sendNoPublicKey(String email) throws MessageSendException {
		try {
			emailService.sendNoPublicKeyMessage(email);
		} catch (RuntimeMessagingException e) {
			logger.error("Could not send a message to " + email, e);
			throw new MessageSendException(e);
		}
	}

	protected void actuallySendTable(UIUser user) throws MessageSendException {
		String filename = hotpProvider.getSequenceForDownloadFileName(user.getUsername());
		// TODO: use streams, not buffers
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			hotpProvider.outputSequenceForDownload(user.getUsername(), baos);
			byte[] bytes = baos.toByteArray();
			
			baos = new ByteArrayOutputStream();
			publicKeyCrypto.encrypt(bytes, filename, user.getPublicKey(), baos);
		} catch (IOException e) {
			// should not get IOException as we're working in-memory
			throw new IllegalStateException(e);
		}
		
		try {
			emailService.sendHOTPTable(user.getEmail(), filename, baos.toByteArray());
		} catch (RuntimeMessagingException e) {
			logger.error("Could not send a message to " + user.getEmail(), e);
			throw new MessageSendException(e);
		}
	}

}
