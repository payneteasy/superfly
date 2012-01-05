package com.payneteasy.superfly.common.pool;

/**
 * <p>
 * Simple pool abstraction. Every pool object is uniquely defined
 * by P (which stands for Parameters - we mean creation parameters
 * here).
 * </p>
 * <p>
 * P instances must implement equals/hashCode methods.
 * </p>
 *
 * @author rpuch
 */
public interface Pool<P, T> {
    T get(P parameters);

    void flushAll();
}
