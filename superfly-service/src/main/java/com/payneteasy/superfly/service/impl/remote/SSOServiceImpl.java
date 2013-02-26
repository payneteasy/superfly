package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.utils.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of SSOService.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOServiceImpl implements SSOService {
	
	private InternalSSOService internalSSOService;
	private HOTPService hotpService;
	private ResetPasswordStrategy resetPasswordStrategy;
	private SubsystemIdentifierObtainer subsystemIdentifierObtainer = new AuthRequestInfoObtainer();

	@Required
	public void setInternalSSOService(InternalSSOService internalSSOService) {
		this.internalSSOService = internalSSOService;
	}

	@Required
	public void setHotpService(HOTPService hotpService) {
		this.hotpService = hotpService;
	}

	@Required
	public void setResetPasswordStrategy(ResetPasswordStrategy resetPasswordStrategy) {
		this.resetPasswordStrategy = resetPasswordStrategy;
	}

	public void setSubsystemIdentifierObtainer(
			SubsystemIdentifierObtainer subsystemIdentifierObtainer) {
		this.subsystemIdentifierObtainer = subsystemIdentifierObtainer;
	}

	/**
	 * @see SSOService#authenticate(String, String, AuthenticationRequestInfo)
	 */
	public SSOUser authenticate(String username, String password,
			AuthenticationRequestInfo authRequestInfo) {
		return internalSSOService.authenticate(username, password,
				obtainSubsystemIdentifier(authRequestInfo.getSubsystemIdentifier()),
				authRequestInfo.getIpAddress(),
				authRequestInfo.getSessionInfo());
	}

	/**
	 * @see SSOService#sendSystemData(String, com.payneteasy.superfly.api.ActionDescription[])
	 */
	public void sendSystemData(String systemIdentifier,
			ActionDescription[] actionDescriptions) {
		internalSSOService.saveSystemData(obtainSubsystemIdentifier(systemIdentifier),
				actionDescriptions);
	}
	
	/**
	 * @see SSOService#getUsersWithActions(String)
	 */
	public List<SSOUserWithActions> getUsersWithActions(
			String subsystemIdentifier) {
		return internalSSOService.getUsersWithActions(
				obtainSubsystemIdentifier(subsystemIdentifier));
	}
	
	/**
	 * @see SSOService#registerUser(String, String, String, String, com.payneteasy.superfly.api.RoleGrantSpecification[], String, String, String, String, String)
	 */
	public void registerUser(String username, String password, String email,
			String subsystemIdentifier, RoleGrantSpecification[] roleGrants,
			String name, String surname, String secretQuestion, String secretAnswer,
			String publicKey)
			throws UserExistsException, PolicyValidationException, BadPublicKeyException, MessageSendException {
		internalSSOService.registerUser(username, password, email,
				obtainSubsystemIdentifier(subsystemIdentifier), roleGrants,
				name, surname, secretQuestion, secretAnswer, publicKey);
	}

	/**
	 * @see SSOService#authenticateUsingHOTP(String, String)
	 */
	public boolean authenticateUsingHOTP(String username, String hotp) {
		String subsystemIdentifier = obtainSubsystemIdentifier(null); // TODO: take default from API
		return internalSSOService.authenticateHOTP(subsystemIdentifier, username, hotp);
	}
	
	protected String obtainSubsystemIdentifier(String systemIdentifier) {
		return subsystemIdentifierObtainer.obtainSubsystemIdentifier(systemIdentifier);
	}

	public void changeTempPassword(String userName, String password) throws PolicyValidationException {
		internalSSOService.changeTempPassword(userName, password);
	}

	/**
	 * @see SSOService#getUserDescription(String)
	 */
	public UserDescription getUserDescription(String username) {
        UserDescription result;
		UserForDescription user = internalSSOService.getUserDescription(username);
        if (user == null) {
            result = null;
        } else {
            result = new UserDescription();
            result.setUsername(user.getUsername());
            result.setEmail(user.getEmail());
            result.setFirstName(user.getName());
            result.setLastName(user.getSurname());
            result.setSecretQuestion(user.getSecretQuestion());
            result.setSecretAnswer(user.getSecretAnswer());
            result.setPublicKey(user.getPublicKey());
        }

        return result;
	}
	
	/**
	 * @see SSOService#resetAndSendOTPTable(String)
	 */
	public void resetAndSendOTPTable(String username) throws UserNotFoundException, MessageSendException {
		resetAndSendOTPTable(null, username);
	}

	/**
	 * @see SSOService#resetAndSendOTPTable(String, String)
	 */
	public void resetAndSendOTPTable(String subsystemIdentifier, String username) throws UserNotFoundException, MessageSendException {
		UserForDescription user = internalSSOService.getUserDescription(username);
		if (user == null) {
			throw new UserNotFoundException(username);
		}
		hotpService.sendTableIfSupported(obtainSubsystemIdentifier(subsystemIdentifier), user.getUserId());
	}

	/**
	 * @see SSOService#updateUserDescription(UserDescription)
	 */
	public void updateUserDescription(UserDescription user)
			throws UserNotFoundException, BadPublicKeyException {
		UserForDescription userForDescription = internalSSOService.getUserDescription(user.getUsername());
		if (userForDescription == null) {
			throw new UserNotFoundException(user.getUsername());
		}
		userForDescription.setEmail(user.getEmail());
		userForDescription.setName(user.getFirstName());
		userForDescription.setSurname(user.getLastName());
		userForDescription.setSecretQuestion(user.getSecretQuestion());
		userForDescription.setSecretAnswer(user.getSecretAnswer());
		userForDescription.setPublicKey(user.getPublicKey());
		internalSSOService.updateUserForDescription(userForDescription);
	}

	/**
	 * @see SSOService#resetPassword(String, String)
	 */
	public void resetPassword(String username, String newPassword)
			throws UserNotFoundException, PolicyValidationException {
		UserForDescription user = internalSSOService.getUserDescription(username);
		if (user == null) {
			throw new UserNotFoundException(username);
		}
		resetPasswordStrategy.resetPassword(user.getUserId(), username, newPassword);
	}

    @Override
    public List<UserStatus> getUserStatuses(List<String> userNames) {
        List<UserWithStatus> daoUsers;
        if (userNames == null) {
            daoUsers = internalSSOService.getUserStatuses(null);
        } else if (userNames.isEmpty()) {
            daoUsers = Collections.emptyList();
        } else {
            daoUsers = internalSSOService.getUserStatuses(
                    StringUtils.collectionToCommaDelimitedString(userNames));
        }
        List<UserStatus> result = new ArrayList<UserStatus>(daoUsers.size());
        for (UserWithStatus daoUser : daoUsers) {
            UserStatus user = new UserStatus();
            user.setUsername(daoUser.getUserName());
            user.setAccountLocked(daoUser.isAccountLocked());
            user.setLoginsFailed(daoUser.getLoginsFailed());
            user.setLastLoginDate(daoUser.getLastLoginDate());
            user.setLastFailedLoginDate(daoUser.getLastFailedLoginDate());
            user.setLastFailedLoginIp(daoUser.getLastFailedLoginIp());
            result.add(user);
        }
        return result;
    }

    @Override
    public SSOUser exchangeSubsystemToken(String subsystemToken) {
        return internalSSOService.exchangeSubsystemToken(subsystemToken);
    }

    @Override
    public void touchSessions(List<Long> sessionIds) {
        internalSSOService.touchSessions(sessionIds);
    }

}
