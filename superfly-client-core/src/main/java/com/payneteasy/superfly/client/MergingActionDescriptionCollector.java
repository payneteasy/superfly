package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ActionDescriptionCollector implementation which takes outputs from several
 * collectors and merges them. First collector in the list has the highest
 * priority.
 *
 * @author Roman Puchkovskiy
 */
public class MergingActionDescriptionCollector implements ActionDescriptionCollector {

    private ActionDescriptionCollector[] collectors;

    public void setCollectors(ActionDescriptionCollector[] collectors) {
        this.collectors = collectors;
    }

    @Override
    public List<ActionDescription> collect() throws CollectionException {
        Map<String, ActionDescription> descriptionsMap = new HashMap<>();
        for (ActionDescriptionCollector collector : collectors) {
            List<ActionDescription> descriptions = collector.collect();
            for (ActionDescription description : descriptions) {
                if (!descriptionsMap.containsKey(description.getName())) {
                    descriptionsMap.put(description.getName(), description);
                }
            }
        }
        return new ArrayList<>(descriptionsMap.values());
    }
}
