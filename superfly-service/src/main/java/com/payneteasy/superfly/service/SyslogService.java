package com.payneteasy.superfly.service;


public interface SyslogService {
	void sendLogMessage(String eventType, boolean success, String resourceIdentity);
}
