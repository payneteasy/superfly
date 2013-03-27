package com.payneteasy.superfly.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoutineResultTest {
    @Test
	public void testStatuses() {
        assertTrue(RoutineResult.okResult().isOk());
        assertFalse(RoutineResult.okResult().isDuplicate());
		assertFalse(RoutineResult.failureResult().isOk());
		assertTrue(RoutineResult.duplicateResult().isDuplicate());
		assertFalse(RoutineResult.duplicateResult().isOk());
	}
}
