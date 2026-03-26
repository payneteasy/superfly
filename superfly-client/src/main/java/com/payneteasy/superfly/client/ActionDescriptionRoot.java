package com.payneteasy.superfly.client;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "actions")
public class ActionDescriptionRoot implements Serializable {
    private List<ActionDescriptionBean> actions = new ArrayList<>();

    public ActionDescriptionRoot() {
    }

    public ActionDescriptionRoot(List<ActionDescriptionBean> actions) {
        this.actions = actions;
    }

    @XmlElement(name = "action")
    public List<ActionDescriptionBean> getActions() {
        return actions;
    }

    public void setActions(List<ActionDescriptionBean> actions) {
        this.actions = actions;
    }
}
