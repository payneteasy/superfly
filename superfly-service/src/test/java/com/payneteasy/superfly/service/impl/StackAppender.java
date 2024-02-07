package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayDeque;
import java.util.Deque;

class StackAppender extends AppenderBase<ILoggingEvent> {

    private final Deque<ILoggingEvent> events = new ArrayDeque<>();

    @Override
    protected void append(ILoggingEvent event) {
        events.addFirst(event);
    }

    Object getLastMessage() {
        if (events.peek() != null) {
            return events.peekFirst().getMessage();
        }
        return null;
    }
}