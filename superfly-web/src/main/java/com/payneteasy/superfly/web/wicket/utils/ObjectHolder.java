package com.payneteasy.superfly.web.wicket.utils;

import java.io.Serializable;

/**
 * Holds an object. Useful when we have to assign something from anonymous
 * inner class.
 * 
 * @author Roman Puchkovskiy
 * @param <T>
 */
public class ObjectHolder<T> implements Serializable {
    private T object;

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
