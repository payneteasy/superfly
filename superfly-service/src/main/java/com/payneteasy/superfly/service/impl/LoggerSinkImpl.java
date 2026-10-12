package com.payneteasy.superfly.service.impl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserInfoService;
import org.springframework.stereotype.Service;

import static com.payneteasy.superfly.common.utils.LogSanitizer.forLog;

@Service
public class LoggerSinkImpl implements LoggerSink {

    private UserInfoService userInfoService;

    @Autowired
    public void setUserInfoService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    public void info(Logger logger, String eventType, boolean success, String resourceIdentity) {
        info(logger, eventType, success, resourceIdentity, null);
    }

    public void info(Logger logger, String eventType, boolean success, String resourceIdentity, String details) {
        String username = userInfoService.getUsername();
        String usernameFormatted;
        if (username == null) {
            usernameFormatted = "<SYSTEM>";
        } else {
            usernameFormatted = username;
        }

        // for easy log parsing
        // user:paynet-local, event:REMOTE_LOGIN, resource:admin, result:success[, details:...][, ip:10.0.0.1]
        StringBuilder message = new StringBuilder();
        message.append("user:").append(forLog(usernameFormatted))
                .append(", event:").append(forLog(eventType))
                .append(", resource:").append(forLog(resourceIdentity))
                .append(", result:").append(success ? "success" : "failure");
        if (details != null) {
            message.append(", details:").append(forLog(details));
        }
        String ip = userInfoService.getRemoteAddress();
        if (ip != null) {
            message.append(", ip:").append(forLog(ip));
        }
        if (success) {
            logger.info(message.toString());
        } else {
            logger.error(message.toString());
        }
    }

}
