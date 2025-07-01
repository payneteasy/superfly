package com.payneteasy.superfly.client.session;


import com.payneteasy.superfly.common.session.HttpSessionWrapper;
import jakarta.servlet.http.HttpServletRequest;

import com.payneteasy.superfly.api.Notifications;
import com.payneteasy.superfly.common.utils.StringUtils;

/**
 * Filter that accepts notifications from the Superfly server.
 *
 * @author Roman Puchkovskiy
 *
 * deprecated see SuperflyLogoutFilter
 */
@Deprecated
public class LogoutNotificationSinkFilter extends AbstractSessionStoreAwareNotificationSinkFilter {

    @Override
    protected boolean acceptsNotificationType(String notificationType) {
        return Notifications.LOGOUT.equals(notificationType);
    }

    @Override
    protected boolean doFilterRequest(HttpServletRequest request) {
        String logoutSessionIds = request.getParameter(getLogoutSessionIdsParameterName());
        if (logoutSessionIds != null) {
            // this is actually a logout request, so process it and
            // interrupt filter chain
            String[] sessionIds = StringUtils.commaDelimitedListToStringArray(logoutSessionIds);
            for (String key : sessionIds) {
                HttpSessionWrapper session = getSessionMapping().removeSessionByKey(key);
                invalidateSessionQuietly(session);
            }
            return true;
        }
        return false;
    }

    protected String getLogoutSessionIdsParameterName() {
        return "superflyLogoutSessionIds";
    }
}
