package com.payneteasy.superfly.web.security.securehandler;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public class AuthorizationException extends Exception {

    public AuthorizationException(String format, Object... argArray) {
        // 1. we don't need stacktrace
        // 2. also if we throw an exception without stacktrace it is very quick
        super(arrayFormat(format, argArray).getMessage(), null, true, false);
    }


}
