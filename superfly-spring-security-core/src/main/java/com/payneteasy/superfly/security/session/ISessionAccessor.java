package com.payneteasy.superfly.security.session;

/**
 * @author rpuch
 */
public interface ISessionAccessor {
    void putAttribute(String key, Object value);

    <T> T getAttribute(String key);

    <T> T removeAttribute(String key);
}
