package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;

import java.util.List;

/**
 * Collects and returns action descriptions.
 *
 * @author Roman Puchkovskiy
 */
public interface ActionDescriptionCollector {
    /**
     * Returns action descriptions.
     *
     * @return action descriptions
     * @throws CollectionException on collection error
     */
    List<ActionDescription> collect() throws CollectionException;
}
