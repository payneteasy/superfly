package com.payneteasy.superfly.service.impl;

import java.util.Stack;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

class StackAppender extends AppenderSkeleton {
	
	private Stack<LoggingEvent> events = new Stack<LoggingEvent>();

	@Override
	protected void append(LoggingEvent event) {
		events.push(event);
	}

	@Override
	public void close() {
		events.clear();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	public Object getLastMessage() {
		return events.peek().getMessage();
	}
}