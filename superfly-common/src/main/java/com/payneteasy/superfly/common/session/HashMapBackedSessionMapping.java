package com.payneteasy.superfly.common.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

/**
 * SessionMapping implementation which uses a HashMap.
 * 
 * @author Roman Puchkovskiy
 */
public class HashMapBackedSessionMapping implements SessionMapping {

    private Map<String, HttpSession> mapping = new HashMap<String, HttpSession>();
    private Map<String, String> idToKey = new HashMap<String, String>();
    private Map<String, String> keyToId = new HashMap<String, String>();

    public synchronized void addSession(String key, HttpSession session) {
        String id = session.getId();
        mapping.put(key, session);
        idToKey.put(id, key);
        keyToId.put(key, id);
    }

    public synchronized HttpSession removeSessionByKey(String key) {
        String id = keyToId.remove(key);
        idToKey.remove(id);
        return mapping.remove(key);
    }

    public synchronized HttpSession removeSessionById(String id) {
        String key = idToKey.remove(id);
        keyToId.remove(key);
        return mapping.remove(key);
    }

    public synchronized Collection<HttpSession> clear() {
        idToKey.clear();
        keyToId.clear();
        Collection<HttpSession> removedSessions = new HashSet<HttpSession>(mapping.values());
        mapping.clear();
        return removedSessions;
    }

}
