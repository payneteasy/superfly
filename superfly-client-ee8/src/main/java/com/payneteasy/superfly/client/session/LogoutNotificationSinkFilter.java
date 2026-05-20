package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.api.Notifications;
import com.payneteasy.superfly.common.session.HttpSessionWrapper;
import com.payneteasy.superfly.common.utils.StringUtils;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter that accepts notifications from the Superfly server (Java EE 8).
 *
 * @deprecated Use {@link SuperflyLogoutFilter}
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
