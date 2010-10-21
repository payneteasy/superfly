package com.payneteasy.superfly.service;

import org.apache.log4j.Logger;

public interface SyslogService {
	void sendLogMessage(Logger logger,String eventType, boolean success, String resourceIdentity);
}
