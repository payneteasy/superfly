package com.payneteasy.superfly.service.impl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserInfoService;
import org.springframework.stereotype.Service;

@Service
public class LoggerSinkImpl implements LoggerSink {

    private UserInfoService userInfoService;

    @Autowired
    public void setUserInfoService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    public void info(Logger logger, String eventType, boolean success, String resourceIdentity) {
        String username = userInfoService.getUsername();
        String usernameFormatted;
        if (username == null) {
            usernameFormatted = "<SYSTEM>";
        } else {
            usernameFormatted = username;
        }

        // for easy log parsing
        // user:paynet-local, event:REMOTE_LOGIN, resource:admin, result:success
        String message = String.format("user:%s, event:%s, resource:%s, result:%s"
                , usernameFormatted, eventType, resourceIdentity, success ? "success" : "failure");
        if(success) {
            logger.info(message);
        } else {
            logger.error(message);
        }
    }

}
