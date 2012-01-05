package com.payneteasy.superfly.common.pool;

import java.util.*;

/**
 * Simple pool implementation which only allows pool to have constrained size.
 *
 * @author rpuch
 */
public abstract class SimplePool<P, T> implements Pool<P, T> {
    private int capacity;

    private final Map<P, Entry> keysToEntries = new HashMap<P, Entry>();
    private final SortedSet<Entry> entries = new TreeSet<Entry>();

    public SimplePool() {
        this(10);
    }

    public SimplePool(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public final synchronized T get(P parameters) {
        T result;

        Entry entry = keysToEntries.get(parameters);
        if (entry != null) {
            entry.accessDate = new Date();
            result = entry.payload;
        } else {
            while (keysToEntries.size() > capacity) {
                Entry oldestEntry = entries.first();
                entries.remove(oldestEntry);
                keysToEntries.remove(oldestEntry.key);
            }

            T newObject = createNew(parameters);
            Entry newEntry = new Entry();
            newEntry.key = parameters;
            newEntry.payload = newObject;
            newEntry.accessDate = new Date();

            keysToEntries.put(parameters, newEntry);
            entries.add(newEntry);

            result = newObject;
        }

        return result;
    }

    @Override
    public final void flushAll() {
        keysToEntries.clear();
        entries.clear();
    }

    protected abstract T createNew(P parameters);

    private class Entry implements Comparable<Entry> {
        private P key;
        private T payload;
        private Date accessDate;

        @Override
        public int compareTo(Entry o) {
            return accessDate.compareTo(o.accessDate);
        }
    }
}
