package com.payneteasy.superfly.model;

import junit.framework.TestCase;

public class RoutineResultTest extends TestCase {
	public void testStatuses() {
		assertTrue(RoutineResult.okResult().isOk());
		assertFalse(RoutineResult.okResult().isDuplicate());
		assertFalse(RoutineResult.failureResult().isOk());
		assertTrue(RoutineResult.duplicateResult().isDuplicate());
		assertFalse(RoutineResult.duplicateResult().isOk());
	}
}
