package com.payneteasy.superfly.jira.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.common.store.UserStore;

public class UserStoreUpdater {
	private static final Logger logger = LoggerFactory.getLogger(UserStoreUpdater.class);
	
	private static final long RETRY_PERIOD_MILLIS = 60 * 1000;
	
	private SSOService ssoService;
	private UserStore userStore;
	private String subsystemIdentifier;
	private String principalName;
	private boolean updating = false;
	private Object updatingMonitor = new Object();
	private Thread updatingThread = null;
	
	public UserStoreUpdater(SSOService ssoService, UserStore userStore,
			String subsystemIdentifier, String principalName) {
		super();
		this.ssoService = ssoService;
		this.userStore = userStore;
		this.subsystemIdentifier = subsystemIdentifier;
		this.principalName = principalName;
	}

	public void runUpdate() {
		if (updating) {
			// first check to return without waiting
			return;
		}
		synchronized (updatingMonitor) {
			if (updating) {
				// second check to avoid race conditions
				return;
			}
			updating = true;
			updatingThread = new Thread(new Runnable() {
				public void run() {
					boolean ok = false;
					while (!ok) {
						try {
							userStore.setUsers(ssoService.getUsersWithActions(subsystemIdentifier, principalName));
							ok = true;
						} catch (Exception e) {
							logger.warn("Exception while getting users", e);
							try {
								Thread.sleep(RETRY_PERIOD_MILLIS);
							} catch (InterruptedException e1) {
								logger.warn("Sleep was interrupted", e1);
							}
						}
					}
					updatingThread = null;
					updating = false;
				}
			});
			updatingThread.start();
		}
	}
	
	public void cleanup() {
		if (updatingThread != null) {
			try {
				updatingThread.interrupt();
			} catch (Exception e) {
				logger.warn("Exception while interrupting updating thread", e);
			}
			updatingThread = null;
		}
	}
}
