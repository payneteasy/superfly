package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.Stack;

class StackAppender extends AppenderBase<ILoggingEvent> {

    private Stack<ILoggingEvent> events = new Stack<>();

    @Override
    protected void append(ILoggingEvent event) {
        events.push(event);
    }

    Object getLastMessage() {
        return events.peek().getMessage();
    }
}