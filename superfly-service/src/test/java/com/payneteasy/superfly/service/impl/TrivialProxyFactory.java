package com.payneteasy.superfly.service.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.payneteasy.superfly.model.RoutineResult;

public class TrivialProxyFactory {
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(Class<T> clazz) {
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz},
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						if (method.getReturnType() == boolean.class) {
							return false;
						}
						if (method.getReturnType() == byte.class) {
							return (byte) 0;
						}
						if (method.getReturnType() == char.class) {
							return (char) 0;
						}
						if (method.getReturnType() == short.class) {
							return (short) 0;
						}
						if (method.getReturnType() == int.class) {
							return (int) 0;
						}
						if (method.getReturnType() == long.class) {
							return (long) 0;
						}
						if (method.getReturnType() == double.class) {
							return (double) 0;
						}
						if (method.getReturnType() == float.class) {
							return (float) 0;
						}
						if (method.getReturnType() == RoutineResult.class) {
							RoutineResult result = new RoutineResult();
							result.setStatus("OK");
							return result;
						}
						return null;
					}
				});
	}
}
