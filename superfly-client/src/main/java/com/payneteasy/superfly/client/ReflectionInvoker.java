package com.payneteasy.superfly.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author rpuch
 */
public class ReflectionInvoker {
    private final Object target;

    public ReflectionInvoker(Object target) {
        if (target == null) {
            throw new IllegalStateException("target is null");
        }
        this.target = target;
    }

    public <T> MethodInvoker<T> method(String methodName, Class<?> ... parameterTypes) {
        final Method method;
        try {
            method = target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No such method: " + methodName, e);
        }
        return new MethodInvoker<T>() {
            @Override
            public T invoke(Object... arguments) {
                try {
                    @SuppressWarnings("unchecked") final T invoke = (T) method.invoke(target, arguments);
                    return invoke;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Cannot invoke", e);
                }
            }
        };
    }
}
