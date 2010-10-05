package com.payneteasy.superfly.service.impl;


import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.service.LoggerSink;

public abstract class AbstractServiceLoggingTest extends TestCase {
	
	protected LoggerSink loggerSink;
	
	public void setUp() {
		loggerSink = EasyMock.createStrictMock(LoggerSink.class);
	}
	
	protected RoutineResult failureResult() {
		RoutineResult result = new RoutineResult();
		result.setStatus("fail");
		return result;
	}
	
	protected RoutineResult okResult() {
		RoutineResult result = new RoutineResult();
		result.setStatus("OK");
		return result;
	}
	
	protected RoutineResult duplicateResult() {
		RoutineResult result = new RoutineResult();
		result.setStatus("duplicate");
		return result;
	}
	
}
