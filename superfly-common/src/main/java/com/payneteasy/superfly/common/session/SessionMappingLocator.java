package com.payneteasy.superfly.common.session;

import com.payneteasy.superfly.common.singleton.SingletonHolder;

/**
 * Used to locate a SessionMapping.
 * 
 * @author Roman Puchkovskiy
 */
public class SessionMappingLocator {
    private static final SingletonHolder<SessionMapping> sessionMappingSingletonHolder = new SingletonHolder<SessionMapping>() {
        @Override
        protected SessionMapping createInstance() {
            return createSessionMapping();
        }
    };

    public static SessionMapping getSessionMapping() {
        return sessionMappingSingletonHolder.getInstance();
    }

    protected static SessionMapping createSessionMapping() {
        return new HashMapBackedSessionMapping();
    }
}
