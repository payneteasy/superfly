package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;
import com.payneteasy.superfly.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ActionDescriptionCollector implementation which just takes a comma-delimited
 * list of action names and breaks it to fragments.
 *
 * @author Roman Puchkovskiy
 */
public class CommaDelimitedListActionDescriptionCollector implements ActionDescriptionCollector {

    private String commaDelimitedList;

    public void setCommaDelimitedList(String commaDelimitedList) {
        this.commaDelimitedList = commaDelimitedList;
    }

    @Override
    public List<ActionDescription> collect() throws CollectionException {
        String[] actionNames = StringUtils.commaDelimitedListToStringArray(commaDelimitedList);
        List<ActionDescription> result = new ArrayList<>(actionNames.length);
        for (String actionName : actionNames) {
            result.add(new ActionDescription(actionName));
        }
        return result;
    }
}
