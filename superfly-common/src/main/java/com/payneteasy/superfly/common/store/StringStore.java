package com.payneteasy.superfly.common.store;

import java.util.Collection;
import java.util.List;

/**
 * Stores strings. Multiple equal instances cannot be stored here.
 * 
 * @author Roman Puchkovskiy
 */
public interface StringStore {
    /**
     * Returns true if the given string exists in the store.
     *
     * @param key    string to check
     * @return true if exists
     */
    boolean exists(String key);
    /**
     * Sets strings to be stored.
     *
     * @param objects    strings
     */
    void setObjects(List<String> objects);
    /**
     * Returns an unmodifiable collection of stored strings.
     *
     * @return strings
     */
    Collection<String> getObjects();
}
