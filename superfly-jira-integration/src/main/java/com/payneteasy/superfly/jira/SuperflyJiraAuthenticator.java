package com.payneteasy.superfly.jira;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;

import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.cookie.CookieFactory;
import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.common.store.UserStore;
import com.payneteasy.superfly.common.store.UserStoreLocator;

/**
 * Superfly-related {@link Authenticator} implementation.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyJiraAuthenticator extends DefaultAuthenticator {
	
	private static final Category log = Category.getInstance(SuperflyJiraAuthenticator.class);
	
	// the age of the autologin cookie - default = 1 year (in seconds)
	private static int AUTOLOGIN_COOKIE_AGE = 365 * 24 * 60 * 60;

	/*
	 * This method implementation was initially taken from DefaultAuthenticator
	 * to pass HttpServletRequest instance to authenticate() method.
	 */
	public boolean login(HttpServletRequest request,
			HttpServletResponse response, String username, String password,
			boolean cookie) throws AuthenticatorException {
		Principal user = getUser(username);

		// check that they can login (they have the USE permission or ADMINISTER
		// permission)
		if (user == null) {
			log.info("Cannot login user '" + username + "' as they do not exist.");
		} else {
			boolean authenticated = authenticate(user, password, request);
			if (authenticated) {
				request.getSession().setAttribute(LOGGED_IN_KEY, user);
				request.getSession().setAttribute(LOGGED_OUT_KEY, null);

				if (getRoleMapper().canLogin(user, request)) {
					if (cookie && response != null) {
						CookieFactory.getCookieHandler().setCookie(request,
								response, getLoginCookieKey(),
								CookieFactory.getCookieEncoder().encodePasswordCookie(username, password, getConfig().getCookieEncoding()),
								AUTOLOGIN_COOKIE_AGE, getCookiePath(request));
					}
					return true;
				} else {
					request.getSession().removeAttribute(LOGGED_IN_KEY);
				}
			} else {
				log.info("Cannot login user '" + username
						+ "' as they used an incorrect password");
			}
		}

		if (response != null && CookieFactory.getCookieHandler().getCookie(request, getLoginCookieKey()) != null) {
			log.warn("User: " + username + " tried to login but they do not have USE permission or weren't found. Deleting cookie.");

			try {
				CookieFactory.getCookieHandler().invalidateCookie(request, response,
						getLoginCookieKey(), getCookiePath(request));
			} catch (Exception e) {
				log.error("Could not invalidate cookie: " + e, e);
			}
		}

		return false;
	}

	protected boolean authenticate(Principal user, String password, HttpServletRequest request) {
		SSOUser ssoUser = SuperflyContextLocator.getSuperflyContext().getSsoService().authenticate(
				user.getName(), password, createAuthRequestInfo(request));
		return ssoUser != null;
	}

	protected AuthenticationRequestInfo createAuthRequestInfo(HttpServletRequest request) {
		AuthenticationRequestInfo requestInfo = new AuthenticationRequestInfo();
		requestInfo.setSubsystemIdentifier(SuperflyContextLocator.getSuperflyContext().getSubsystemIdentifier());
		requestInfo.setIpAddress(request.getRemoteAddr());
		requestInfo.setSessionInfo(createSessionInfo(request));
		return requestInfo;
	}

	protected String createSessionInfo(HttpServletRequest request) {
		return null;
	}

	protected UserStore getUserStore() {
		return UserStoreLocator.getUserStore();
	}
	
	protected SSOUserWithActions getUserByName(String username) {
		return getUserStore().getUser(username);
	}
}
