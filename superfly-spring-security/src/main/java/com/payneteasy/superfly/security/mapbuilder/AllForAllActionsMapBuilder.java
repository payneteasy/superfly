package com.payneteasy.superfly.security.mapbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;

/**
 * {@link ActionsMapBuilder} which gives every role the same actions.
 * 
 * @author Roman Puchkovskiy
 */
public class AllForAllActionsMapBuilder implements ActionsMapBuilder {

    private List<String> roleNames = new ArrayList<String>();
    private ActionsSource actionsSource;

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    @Required
    public void setActionsSource(ActionsSource actionsSource) {
        this.actionsSource = actionsSource;
    }

    public Map<SSORole, SSOAction[]> build() throws Exception {
        Map<SSORole, SSOAction[]> map = new HashMap<SSORole, SSOAction[]>();
        for (String roleName : roleNames) {
            SSORole role = new SSORole(roleName);
            SSOAction[] actions = actionsSource.getActions();
            map.put(role, actions);
        }
        return map;
    }

}
