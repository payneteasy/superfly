package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;

public class SortRoleOfSubsystem implements Serializable{
    private List<UIRoleWithActions> roleWithAction;

    public List<UIRoleWithActions> getRoleWithAction() {
        return roleWithAction;
    }

    public void setRoleWithAction(List<UIRoleWithActions> roleWithAction) {
        this.roleWithAction = roleWithAction;
    }

    List<String> getSubsystemsName() {
        List<String> sub = new ArrayList<String>();
        for (UIRoleWithActions rwa : roleWithAction) {
            if (!sub.contains(rwa.getSubsystemName())) {
                sub.add(rwa.getSubsystemName());
            }
        }
        return sub;
    }
    List<UIRoleWithActions> getRoles(String subsystemName){
        List<UIRoleWithActions> roles = new ArrayList<UIRoleWithActions>();
        for(UIRoleWithActions rwa: roleWithAction){
            if(subsystemName.equals(rwa.getSubsystemName())){
                if(!roles.contains(rwa)){

                    roles.add(rwa);
                }
            }
        }
        return roles;
    }

}
