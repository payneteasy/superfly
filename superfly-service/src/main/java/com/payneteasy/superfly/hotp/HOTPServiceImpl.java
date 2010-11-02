package com.payneteasy.superfly.hotp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPService;

@Transactional
public class HOTPServiceImpl implements HOTPService {
	
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

	public void sendTableIfSupported(long userId) {
		obtainUserIfNeededAndSendTableIfSupported(userId, null);
	}
	
	public void resetTableAndSendIfSupported(long userId) {
		UIUser user = userDao.getUser(userId);
		hotpProvider.resetSequence(user.getUsername());
		obtainUserIfNeededAndSendTableIfSupported(userId, user);
	}
	
	private void obtainUserIfNeededAndSendTableIfSupported(long userId, UIUser user) {
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

	protected void sendNoPublicKey(String email) {
		emailService.sendNoPublicKeyMessage(email);
	}

	protected void actuallySendTable(UIUser user) {
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
		
		emailService.sendHOTPTable(user.getEmail(), filename, baos.toByteArray());
	}

}
