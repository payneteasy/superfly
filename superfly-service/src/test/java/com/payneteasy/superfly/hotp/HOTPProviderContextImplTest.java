package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.service.impl.TrivialProxyFactory;
import com.payneteasy.superfly.spisupport.HOTPDao;
import com.payneteasy.superfly.spisupport.ObjectResolver;

import junit.framework.TestCase;

public class HOTPProviderContextImplTest extends TestCase {
	public void test() {
		final HOTPDao hotpDao = TrivialProxyFactory.createProxy(HOTPDao.class);
		ObjectResolver resolver = new ObjectResolver() {
			@SuppressWarnings("unchecked")
			public <T> T resolve(Class<T> clazz) {
				if (clazz == HOTPDao.class) {
					return (T) hotpDao;
				}
				return null;
			}
		};
		HOTPProviderContextImpl context = new HOTPProviderContextImpl(resolver, "key", 7, 11, 1000);
		
		assertSame(resolver, context.getObjectResolver());
		assertEquals("key", context.getMasterKey());
		assertEquals(7, context.getCodeDigits());
		assertEquals(11, context.getLookahead());
		assertEquals(1000, context.getTableSize());
		assertNotNull(context.getHOTPDao());
	}
}
