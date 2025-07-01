package com.payneteasy.superfly.common.session;

import com.payneteasy.superfly.common.singleton.SingletonHolder;

/**
 * Used to locate a SessionMapping.
 *
 * @author Roman Puchkovskiy
 */
public class SessionMappingLocator {

    private static final SingletonHolder<SessionMapping<HttpSessionWrapper>> sessionMappingSingletonHolder =
            new SingletonHolder<>() {
                @Override
                protected SessionMapping<HttpSessionWrapper> createInstance() {
                    return createSessionMapping();
                }
            };

    public static SessionMapping<HttpSessionWrapper> getSessionMapping() {
        return sessionMappingSingletonHolder.getInstance();
    }

    protected static SessionMapping<HttpSessionWrapper> createSessionMapping() {
        return new HashMapBackedSessionMapping<>(HttpSessionWrapper::getId);
    }
}
