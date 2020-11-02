package com.payneteasy.superfly.notification.strategy;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.notification.NotificationException;

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
            ParameterSetter parameterSetter) throws NotificationException {
        PostMethod httpMethod = new PostMethod(uri);
        httpMethod.setParameter("superflyNotification", notificationType);
        parameterSetter.setParameters(httpMethod);
        try {
            httpClient.executeMethod(httpMethod);
            if (logger.isInfoEnabled()) {
                logger.info("Successfully notified " + uri + " with params " + Arrays.asList(httpMethod.getParameters()));
            }
        } catch (HttpException e) {
            throw new NotificationException(e);
        } catch (IOException e) {
            throw new NotificationException(e);
        } finally {
            try {
                httpMethod.releaseConnection();
            } catch (IllegalArgumentException ignored) {
                logger.warn(ignored.getMessage(), ignored);
            }
        }
    }

    @Required
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected static interface ParameterSetter {
        void setParameters(PostMethod httpMethod);
    }
}
