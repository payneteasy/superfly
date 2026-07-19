package com.payneteasy.superfly.security.mapbuilder;

import java.util.List;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.client.ActionDescriptionCollector;

/**
 * Obtains actions from {@link ActionDescriptionCollector}.
 *
 * @author Roman Puchkovskiy
 */
@Setter
public class CollectingActionsSource implements ActionsSource {

    private ActionDescriptionCollector actionDescriptionCollector;

    public SSOAction[] getActions() throws Exception {
        List<ActionDescription> descriptions = actionDescriptionCollector.collect();
        SSOAction[] actions = new SSOAction[descriptions.size()];
        for (int i = 0; i < descriptions.size(); i++) {
            actions[i] = new SSOAction(descriptions.get(i).getName(), false);
        }
        return actions;
    }

}
