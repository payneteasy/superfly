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

    /**
     * Default constructor that leaves the held object null.
     */
    public ObjectHolder() {
    }

    /**
     * Constructor which initializes the object to hold.
     *
     * @param value object to hold
     */
    public ObjectHolder(T value) {
        object = value;
    }

    /**
     * Returns the held object.
     *
     * @return object
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the object to hold.
     *
     * @param object object to hold
     */
    public void setObject(T object) {
        this.object = object;
    }
}
