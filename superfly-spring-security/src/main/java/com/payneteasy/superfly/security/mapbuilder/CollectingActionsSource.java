package com.payneteasy.superfly.security.mapbuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.client.ActionDescriptionCollector;

/**
 * Obtains actions from {@link ActionDescriptionCollector}.
 * 
 * @author Roman Puchkovskiy
 */
public class CollectingActionsSource implements ActionsSource {

    private ActionDescriptionCollector actionDescriptionCollector;

    @Required
    public void setActionDescriptionCollector(
            ActionDescriptionCollector actionDescriptionCollector) {
        this.actionDescriptionCollector = actionDescriptionCollector;
    }

    public SSOAction[] getActions() throws Exception {
        List<ActionDescription> descriptions = actionDescriptionCollector.collect();
        SSOAction[] actions = new SSOAction[descriptions.size()];
        for (int i = 0; i < descriptions.size(); i++) {
            actions[i] = new SSOAction(descriptions.get(i).getName(), false);
        }
        return actions;
    }

}
