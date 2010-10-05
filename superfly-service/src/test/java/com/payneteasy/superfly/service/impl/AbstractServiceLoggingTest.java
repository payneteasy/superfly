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
		return RoutineResult.failureResult();
	}
	
	protected RoutineResult okResult() {
		return RoutineResult.okResult();
	}
	
	protected RoutineResult duplicateResult() {
		return RoutineResult.duplicateResult();
	}
	
}
