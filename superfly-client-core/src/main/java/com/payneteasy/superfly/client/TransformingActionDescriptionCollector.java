package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;

import java.util.List;

/**
 * This implementation decorates another {@link ActionDescriptionCollector}
 * implementation and transforms action names using transformers given to it.
 *
 * @author Roman Puchkovskiy
 */
public class TransformingActionDescriptionCollector implements ActionDescriptionCollector {

    private ActionDescriptionCollector collector;
    private StringTransformer[] transformers;

    public void setCollector(ActionDescriptionCollector collector) {
        this.collector = collector;
    }

    public void setTransformers(StringTransformer[] transformers) {
        this.transformers = transformers;
    }

    @Override
    public List<ActionDescription> collect() throws CollectionException {
        List<ActionDescription> list = collector.collect();
        for (ActionDescription actionDescription : list) {
            actionDescription.setName(applyTransformers(actionDescription.getName()));
        }
        return list;
    }

    protected String applyTransformers(String name) {
        for (StringTransformer transformer : transformers) {
            name = transformer.transform(name);
        }
        return name;
    }
}
