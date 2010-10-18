package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.model.RoutineResult;

import junit.framework.TestCase;

public class TrivialProxyFactoryTest extends TestCase {
	public void testProxy() {
		I i = TrivialProxyFactory.createProxy(I.class);
		i.voidMethod();
		i.nonVoidMethod();
		i.intMethod();
		i.booleanMethod();
		i.charMethod();
	}
	
	public void testProxyForRoutineResult() {
		J j = TrivialProxyFactory.createProxy(J.class);
		RoutineResult result = j.getResult();
		assertNotNull(result);
		assertTrue(result.isOk());
	}
	
	private static interface I {
		void voidMethod();
		Object nonVoidMethod();
		int intMethod();
		boolean booleanMethod();
		char charMethod();
	}
	
	private static interface J {
		RoutineResult getResult();
	}
}
