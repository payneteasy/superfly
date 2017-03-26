package com.payneteasy.superfly.common.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple {@link StringStore} implementation which users an in-memory set.
 * 
 * @author Roman Puchkovskiy
 */
public class SimpleStringStore implements StringStore {

    private Set<String> strings;

    public boolean exists(String key) {
        return strings.contains(key);
    }

    public void setObjects(List<String> objects) {
        strings = Collections.unmodifiableSet(new HashSet<String>(objects));
    }

    public Collection<String> getObjects() {
        return strings;
    }

}
