package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;

import java.io.Serial;


public class ActionDescriptionBean extends ActionDescription {
    @Serial
    private static final long serialVersionUID = -513189437457390542L;

    public ActionDescriptionBean() {
    }

    public ActionDescriptionBean(String name) {
        super(name);
    }

    public ActionDescriptionBean(String name, String description) {
        super(name, description);
    }
}
