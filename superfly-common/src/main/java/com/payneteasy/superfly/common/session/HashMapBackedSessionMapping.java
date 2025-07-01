package com.payneteasy.superfly.common.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

/**
 * SessionMapping implementation which uses a HashMap.
 *
 * @author Roman Puchkovskiy
 * @param <S> the session type
 */
public class HashMapBackedSessionMapping<S> implements SessionMapping<S> {

    private final Map<String, S> mapping = new HashMap<>();
    private final Map<String, String> idToKey = new HashMap<>();
    private final Map<String, String> keyToId = new HashMap<>();
    private final Function<S, String> sessionIdExtractor;

    public HashMapBackedSessionMapping(Function<S, String> sessionIdExtractor) {
        this.sessionIdExtractor = sessionIdExtractor;
    }

    @Override
    public synchronized void addSession(String key, S session) {
        String id = sessionIdExtractor.apply(session);
        mapping.put(key, session);
        idToKey.put(id, key);
        keyToId.put(key, id);
    }

    @Override
    public synchronized S removeSessionByKey(String key) {
        String id = keyToId.remove(key);
        if (id != null) {
            idToKey.remove(id);
        }
        return mapping.remove(key);
    }

    @Override
    public synchronized void removeSessionById(String id) {
        String key = idToKey.remove(id);
        if (key != null) {
            keyToId.remove(key);
            mapping.remove(key);
        }
    }

    @Override
    public synchronized Collection<S> clear() {
        idToKey.clear();
        keyToId.clear();
        Collection<S> removedSessions = new HashSet<>(mapping.values());
        mapping.clear();
        return removedSessions;
    }

}
