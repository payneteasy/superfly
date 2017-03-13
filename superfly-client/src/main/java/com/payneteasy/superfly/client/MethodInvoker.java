package com.payneteasy.superfly.client;

/**
 * @author rpuch
 */
public interface MethodInvoker<R> {
    R invoke(Object ... arguments);
}
