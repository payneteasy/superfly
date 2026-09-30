package com.payneteasy.superfly.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "actions")
public class ActionDescriptionRoot implements Serializable {
    private List<ActionDescriptionBean> actions = new ArrayList<>();

    public ActionDescriptionRoot() {
    }

    public ActionDescriptionRoot(List<ActionDescriptionBean> actions) {
        this.actions = actions;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "action")
    public List<ActionDescriptionBean> getActions() {
        return actions;
    }

    public void setActions(List<ActionDescriptionBean> actions) {
        this.actions = actions;
    }
}
