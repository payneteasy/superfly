package com.payneteasy.superfly.common.singleton;

/**
 * Utility class that holds a singleton instance. Its implementation prevents
 * a double-lock problem.
 * 
 * @author Roman Puchkovskiy
 * @param <T>
 */
public abstract class SingletonHolder<T> {
    private T instance;

    /**
     * Obtains a singleton instance held by this object.
     *
     * @return instance
     */
    public T getInstance() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    // this is to prevent double object creation if one thread
                    // blocks another one here
                    instance = createInstance();
                }
            }
        }
        return instance;
    }

    /**
     * Actually creates a new instance.
     *
     * @return created instance
     */
    protected abstract T createInstance();
}
