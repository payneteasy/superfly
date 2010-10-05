package com.payneteasy.superfly.service;

import org.slf4j.Logger;

public interface LoggerSink {
	void info(Logger logger, String eventType, boolean success, String resourceIdentity);
}
