package com.payneteasy.superfly.service.impl;


import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.service.LoggerSink;
import org.easymock.EasyMock;
import org.junit.Before;

public abstract class AbstractServiceLoggingTest {
	
	protected LoggerSink loggerSink;

    @Before
	public void init() {
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
