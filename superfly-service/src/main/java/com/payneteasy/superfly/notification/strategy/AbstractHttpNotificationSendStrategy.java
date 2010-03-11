package com.payneteasy.superfly.notification.strategy;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base for any send strategy using HTTP to deliver notifications.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractHttpNotificationSendStrategy implements
		NotificationSendStrategy {
	
	protected static Logger logger = LoggerFactory.getLogger(AbstractHttpNotificationSendStrategy.class);

	protected HttpClient httpClient;

	protected void doCall(String uri, String notificationType,
			ParameterSetter parameterSetter) {
		PostMethod httpMethod = new PostMethod(uri);
		httpMethod.setParameter("superflyNotification", notificationType);
		parameterSetter.setParameters(httpMethod);
		try {
			httpClient.executeMethod(httpMethod);
		} catch (HttpException e) {
			throw new WrapperException(e);
		} catch (IOException e) {
			throw new WrapperException(e);
		} catch (RuntimeException e) {
			throw new WrapperException(e);
		} finally {
			try {
				httpMethod.releaseConnection();
			} catch (IllegalArgumentException ignored) {
				logger.warn(ignored.getMessage(), ignored);
			}
		}
	}
	
	protected void doExecute(final Runnable runnable) {
		new Thread(new Runnable() {
			public void run() {
				try {
					runnable.run();
				} catch (WrapperException e) {
					Throwable cause = e.getCause();
					logger.error(cause.getMessage(), cause);
				} catch (RuntimeException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}).start();
	}

	@Required
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	protected static interface ParameterSetter {
		void setParameters(PostMethod httpMethod);
	}
	
	protected static class WrapperException extends RuntimeException {

		public WrapperException() {
			super();
		}

		public WrapperException(String message, Throwable cause) {
			super(message, cause);
		}

		public WrapperException(String message) {
			super(message);
		}

		public WrapperException(Throwable cause) {
			super(cause);
		}
		
	}

}
