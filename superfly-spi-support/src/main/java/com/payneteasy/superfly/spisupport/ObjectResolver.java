package com.payneteasy.superfly.spisupport;

/**
 * Used to resolve provider dependencies.
 *
 * @author Roman Puchkovskiy
 */
public interface ObjectResolver {
    /**
     * Resolves dependency by its required class/interface.
     *
     * @param <T>        required type
     * @param clazz        class/interface by which to resolve
     * @return resolved instance or null
     */
    <T> T resolve(Class<T> clazz);
}
